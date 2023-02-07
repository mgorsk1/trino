/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.likematcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

final class NFA
{
    private final State start;
    private final State accept;
    private final List<State> states;
    private final List<List<Transition>> transitions;

    private NFA(State start, State accept, List<State> states, List<List<Transition>> transitions)
    {
        this.start = requireNonNull(start, "start is null");
        this.accept = requireNonNull(accept, "accept is null");
        this.states = requireNonNull(states, "states is null");
        this.transitions = requireNonNull(transitions, "transitions is null");
    }

    public DFA toDfa()
    {
        Map<Set<State>, DFA.State> activeStates = new HashMap<>();

        DFA.Builder builder = new DFA.Builder();
        DFA.State failed = builder.addFailState();
        for (int i = 0; i < 256; i++) {
            builder.addTransition(failed, i, failed);
        }

        Set<State> initial = Set.of(this.start);
        Queue<Set<State>> queue = new ArrayDeque<>();
        queue.add(initial);

        DFA.State dfaStartState = builder.addStartState(makeLabel(initial), initial.contains(accept));
        activeStates.put(initial, dfaStartState);

        Set<Set<State>> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Set<State> current = queue.poll();

            if (!visited.add(current)) {
                continue;
            }

            // For each possible byte value...
            for (int byteValue = 0; byteValue < 256; byteValue++) {
                Set<State> next = new HashSet<>();
                for (State nfaState : current) {
                    for (Transition transition : transitions(nfaState)) {
                        Condition condition = transition.condition();
                        State target = states.get(transition.target());

                        if (condition instanceof Value valueTransition && valueTransition.value() == (byte) byteValue) {
                            next.add(target);
                        }
                        else if (condition instanceof Prefix prefixTransition) {
                            if (byteValue >>> (8 - prefixTransition.bits()) == prefixTransition.prefix()) {
                                next.add(target);
                            }
                        }
                    }
                }

                DFA.State from = activeStates.get(current);
                DFA.State to = failed;
                if (!next.isEmpty()) {
                    to = activeStates.computeIfAbsent(next, nfaStates -> builder.addState(makeLabel(nfaStates), nfaStates.contains(accept)));
                    queue.add(next);
                }
                builder.addTransition(from, byteValue, to);
            }
        }

        return builder.build();
    }

    private List<Transition> transitions(State state)
    {
        return transitions.get(state.id());
    }

    private String makeLabel(Set<State> states)
    {
        return "{" + states.stream()
                .map(State::id)
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(",")) + "}";
    }

    public static class Builder
    {
        private int nextId;
        private State start;
        private State accept;
        private final List<State> states = new ArrayList<>();
        private final List<List<Transition>> transitions = new ArrayList<>();

        public State addState()
        {
            State state = new State(nextId++);
            states.add(state);
            transitions.add(new ArrayList<>());
            return state;
        }

        public State addStartState()
        {
            checkState(start == null, "Start state is already set");
            start = addState();
            return start;
        }

        public void setAccept(State state)
        {
            checkState(accept == null, "Accept state is already set");
            accept = state;
        }

        public void addTransition(State from, Condition condition, State to)
        {
            transitions.get(from.id()).add(new Transition(to.id(), condition));
        }

        public NFA build()
        {
            return new NFA(start, accept, states, transitions);
        }
    }

    public record State(int id)
    {
        @Override
        public String toString()
        {
            return "(" + id + ")";
        }
    }

    record Transition(int target, Condition condition) {}

    sealed interface Condition
            permits Value, Prefix
    {
    }

    record Value(byte value)
            implements Condition {}

    record Prefix(int prefix, int bits)
            implements Condition {}
}
