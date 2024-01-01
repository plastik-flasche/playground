import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

enum ValidCharacters {
	BROKEN('#'),
	FUNCTIONAL('.'),
	UNKNOWN('?');

	final char symbol;

	ValidCharacters(char symbol) {
		this.symbol = symbol;
	}

	static ValidCharacters from(int symbol) {
		return Arrays.stream(values()).filter(c -> c.symbol == symbol).findFirst().orElseThrow();
	}

	@Override
	public String toString() {
		return String.valueOf(symbol);
	}
}

class Transition<T> {
	private final T match;
	private final State<T> nextState;

	public Transition(T match, State<T> nextState) {
		this.match = match;
		this.nextState = nextState;
	}

	public boolean matches(List<T> input) {
		if (match == null) {
			return true;
		}
		if (input.isEmpty()) {
			return false;
		}
		return input.get(0).equals(match);
	}

	public boolean isEmptyMatching() {
		return match == null;
	}

	public boolean matchesAny(Set<List<T>> inputs) {
		return inputs.stream().anyMatch(this::matches);
	}

	public State<T> nextState() {
		return nextState;
	}

	@Override
	public String toString() {
		return "[" + match + "] -> " + nextState.hashCode();
	}
}

class State<T> {
	private final List<Transition<T>> transitions;
	private boolean isAccepting;

	private final Map<List<T>, Long> groups;

	public State(List<Transition<T>> transitions, boolean isAccepting) {
		this.transitions = transitions;
		this.isAccepting = isAccepting;
		this.groups = new HashMap<>();
	}

	public State(boolean isAccepting) {
		this(new ArrayList<>(), isAccepting);
	}

	public State() {
		this(false);
	}

	public boolean canTransitionAny() {
		return transitions.stream().anyMatch(t -> t.matchesAny(groups.keySet()));
	}

	public void addGroup(List<T> group) {
		addGroup(group, 1L);
	}

	public void addGroup(List<T> group, long count) {
		groups.put(group, groups.getOrDefault(group, 0L) + count);
	}

	public void transitionAll() {
		for (var entry : groups.entrySet()) {
			List<T> group = entry.getKey();
			boolean isEmpty = group.isEmpty();
			boolean transitioned = false;
			List<T> groupWithoutFirst = isEmpty ? group : group.subList(1, group.size());
			for (Transition<T> transition : transitions) {
				if (transition.matches(group)) {
					transitioned = true;
					if (transition.isEmptyMatching()) {
						transition.nextState().addGroup(group, entry.getValue());
					} else {
						transition.nextState().addGroup(groupWithoutFirst, entry.getValue());
					}
				}
			}
			if (transitioned || !isEmpty) {
				groups.remove(group);
			}
		}
	}

	public long getFinalGroupsCount() {
		return groups.keySet().stream().filter(g -> g.isEmpty()).mapToLong(groups::get).sum();
	}

	public void clearGroups() {
		groups.clear();
	}

	public boolean isAccepting() {
		return isAccepting;
	}

	public void addTransition(Transition<T> transition) {
		transitions.add(transition);
	}

	public void setAccepting(boolean isAccepting) {
		this.isAccepting = isAccepting;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("State(" + hashCode() + ") {");
		sb.append("\taccepting: " + isAccepting + ", ");
		for (Transition<T> transition : transitions) {
			sb.append("\t" + transition + ", ");
		}
		sb.append("}");
		return sb.toString();
	}
}

class StateMachine<T> {
	private final List<State<T>> states;
	private final State<T> startState;

	public StateMachine(State<T> starState) {
		this.states = new ArrayList<>();
		this.startState = starState;
		this.states.add(starState);
	}

	public boolean isFinal() {
		return states.stream().allMatch(state -> !state.canTransitionAny());
	}

	public void transitionUntilFinal() {
		while (!isFinal()) {
			states.forEach(State::transitionAll);
		}
	}

	public long countAccepting() {
		return states.stream().filter(State::isAccepting).mapToLong(State::getFinalGroupsCount).sum();
	}

	public void clearGroups() {
		states.forEach(State::clearGroups);
	}

	public void addState(State<T> state) {
		states.add(state);
	}

	public State<T> getStartState() {
		return startState;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("StateMachine {\n");
		sb.append("\tstartState: " + startState.hashCode() + "\n");
		for (State<T> state : states) {
			sb.append("\t" + state + "\n");
		}
		sb.append("}");
		return sb.toString();
	}
}

class CombinationNumberFinder {
	StateMachine<ValidCharacters> machine;

	private void addBroken(List<State<ValidCharacters>> states) {
		states.add(new State<>());
		states.get(states.size() - 2)
				.addTransition(new Transition<>(ValidCharacters.BROKEN, states.get(states.size() - 1)));
		states.get(states.size() - 2)
				.addTransition(new Transition<>(ValidCharacters.UNKNOWN, states.get(states.size() - 1)));
	}

	private void addFunctional(List<State<ValidCharacters>> states) {
		states.add(new State<>());
		states.get(states.size() - 2)
				.addTransition(new Transition<>(ValidCharacters.FUNCTIONAL, states.get(states.size() - 1)));
		states.get(states.size() - 2)
				.addTransition(new Transition<>(ValidCharacters.UNKNOWN, states.get(states.size() - 1)));
		states.get(states.size() - 1)
				.addTransition(new Transition<>(ValidCharacters.FUNCTIONAL, states.get(states.size() - 1)));
		states.get(states.size() - 1)
				.addTransition(new Transition<>(ValidCharacters.UNKNOWN, states.get(states.size() - 1)));
	}

	public CombinationNumberFinder(List<Long> numbers) {
		State<ValidCharacters> startState = new State<>();
		startState.addTransition(new Transition<>(ValidCharacters.FUNCTIONAL, startState));
		startState.addTransition(new Transition<>(ValidCharacters.UNKNOWN, startState));
		List<State<ValidCharacters>> states = new ArrayList<>();
		states.add(startState);
		for (int i = 0; i < numbers.size(); i++) {
			for (int j = 0; j < numbers.get(i); j++) {
				addBroken(states);
			}
			if (i < numbers.size() - 1) {
				addFunctional(states);
			}
		}
		// make last state accepting
		states.get(states.size() - 1).setAccepting(true);
		// remove first state
		states.remove(0);

		// add transition from end to end: functional, unknown
		states.get(states.size() - 1)
				.addTransition(new Transition<>(ValidCharacters.FUNCTIONAL, states.get(states.size() - 1)));
		states.get(states.size() - 1)
				.addTransition(new Transition<>(ValidCharacters.UNKNOWN, states.get(states.size() - 1)));

		machine = new StateMachine<>(startState);
		states.forEach(machine::addState);
	}

	public long getMatches(String input) {
		return getMatches(input.chars().mapToObj(ValidCharacters::from).toList());
	}

	public long getMatches(Collection<ValidCharacters> input) {
		machine.getStartState().addGroup(new ArrayList<>(input));
		machine.transitionUntilFinal();
		long result = machine.countAccepting();
		machine.clearGroups();
		return result;
	}
}

public class Day12 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "12", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<FunctionRecord> records = lines.stream().map(FunctionRecord::from).toList();

		long task1 = records.stream().mapToLong(Day12::getPossibleCombinationCount).sum();
		System.out.println("Task 1: " + task1);

		long task2 = records.stream().mapToLong(r -> getPossibleCombinationCount(r.multiplyEntries(5))).sum();
		System.out.println("Task 2: " + task2);
	}

	record FunctionRecord(List<ValidCharacters> characters, List<Long> numbers) {
		public static FunctionRecord from(String line) {
			String[] parts = line.split(" ");
			List<ValidCharacters> characters = parts[0].chars().mapToObj(ValidCharacters::from).toList();
			List<Long> numbers = Arrays.stream(parts[1].split(",")).map(Long::parseLong).toList();
			return new FunctionRecord(characters, numbers);
		}

		public FunctionRecord multiplyEntries(int count) {
			if (count == 0) {
				throw new IllegalArgumentException("count must be greater than 0");
			}
			if (count == 1) {
				return this;
			}
			List<ValidCharacters> newCharacters = new ArrayList<>();
			List<Long> newNumbers = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				newCharacters.addAll(characters);
				newCharacters.add(ValidCharacters.UNKNOWN);
				newNumbers.addAll(numbers);
			}
			newCharacters.remove(newCharacters.size() - 1);
			return new FunctionRecord(newCharacters, newNumbers);
		}

		@Override
		public String toString() {
			return characters.stream().map(ValidCharacters::toString).collect(Collectors.joining())
					+ " "
					+ numbers.stream().map(Object::toString).collect(Collectors.joining(","));
		}
	}

	public static long getPossibleCombinationCount(FunctionRecord record) {
		CombinationNumberFinder finder = new CombinationNumberFinder(record.numbers());
		return finder.getMatches(record.characters());
	}
}
