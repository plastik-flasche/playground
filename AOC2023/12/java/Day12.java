import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class Day12 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "12", "TEST.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		long startTime = System.nanoTime();
		lines.stream()
				.map(Day12::compileLine)
				.map(line -> line.multiplyEntries(7))
				.map(Day12::calculateCombinations)
				.forEach(System.out::println);
		long endTime = System.nanoTime();
		System.out.println("Time taken: " + (endTime - startTime) / 1000000 + "ms");
	}

	static class Line {
		private final List<ValidCharacters> characters;
		private final List<Integer> numbers;

		public static Line fromGroups(List<List<ValidCharacters>> groups, List<Integer> numbers) {
			List<ValidCharacters> characters = new ArrayList<>();
			for (List<ValidCharacters> group : groups) {
				characters.addAll(group);
				characters.add(ValidCharacters.DOT);
			}
			if (!groups.isEmpty()) {
				characters.remove(characters.size() - 1);
			}
			return new Line(characters, numbers);
		}

		public Line(List<ValidCharacters> characters, List<Integer> numbers) {
			this.characters = characters;
			this.numbers = numbers;
		}

		public Line multiplyEntries(int multiplier) {
			if (multiplier < 1) {
				throw new IllegalArgumentException("Multiplier must be at least 1");
			}

			List<ValidCharacters> multipliedChars = new ArrayList<>();
			for (int i = 0; i < multiplier; i++) {
				multipliedChars.addAll(this.characters);
				multipliedChars.add(ValidCharacters.QUESTION_MARK);
			}
			multipliedChars.remove(multipliedChars.size() - 1);

			List<Integer> multipliedNumbers = new ArrayList<>();
			for (int i = 0; i < multiplier; i++) {
				multipliedNumbers.addAll(this.numbers);
			}

			return new Line(multipliedChars, multipliedNumbers);
		}

		public List<ValidCharacters> getCharacters() {
			return new ArrayList<>(characters);
		}

		public List<List<ValidCharacters>> groups() {
			List<List<ValidCharacters>> groups = new ArrayList<>();
			List<ValidCharacters> currentGroup = new ArrayList<>();
			for (ValidCharacters character : this.characters) {
				if (character == ValidCharacters.DOT) {
					if (!currentGroup.isEmpty()) {
						groups.add(currentGroup);
						currentGroup = new ArrayList<>();
					}
				} else {
					currentGroup.add(character);
				}
			}
			if (!currentGroup.isEmpty()) {
				groups.add(currentGroup);
			}
			return groups;
		}

		public List<Integer> numbers() {
			return new ArrayList<>(numbers);
		}

		@Override
		public String toString() {
			String charactersString = this.characters.stream()
					.map(ValidCharacters::toString)
					.collect(Collectors.joining());
			String numbersString = this.numbers.stream()
					.map(Object::toString)
					.collect(Collectors.joining(","));
			return charactersString + " " + numbersString;
		}
	}

	public static Line compileLine(String line) {
		String[] parts = line.split(" ");

		List<ValidCharacters> characters = parts[0].chars()
				.mapToObj(c -> ValidCharacters.fromChar((char) c))
				.toList();

		String[] numberStrings = parts[1].split(",");
		List<Integer> numbers = Stream.of(numberStrings).map(Integer::parseInt).toList();

		return new Line(characters, numbers);
	}

	public static int calculateCombinations(Line line) {
		List<List<ValidCharacters>> groups = line.groups();
		List<Integer> numbers = line.numbers();

		if (groups.size() == 0) {
			if (numbers.size() == 0) {
				return 1;
			} else {
				return 0;
			}
		}

		List<ValidCharacters> firstGroup = groups.get(0);
		List<List<ValidCharacters>> nextGroups = groups.subList(1, groups.size());

		List<Combination> thisPass = findAllPossibleNumberCombinations(firstGroup, numbers);

		if (numbers.size() == 0) {
			Combination emptyCombination = thisPass.stream()
					.filter(combination -> combination.numbers.size() == 0)
					.findFirst()
					.orElseThrow(() -> new RuntimeException("No empty combination found"));
			return emptyCombination.multiplier;
		}

		int sum = 0;
		for (Combination combination : thisPass) {
			// remove first combination.numbers.size() elements
			List<Integer> remainingNumbers = numbers.subList(combination.numbers.size(), numbers.size());
			sum += combination.multiplier * calculateCombinations(Line.fromGroups(nextGroups, remainingNumbers));
		}

		return sum;
	}

	static record Combination(List<Integer> numbers, int multiplier) {
		public int numberOfHashes() {
			return numbers.stream().mapToInt(Integer::intValue).sum();
		}
	}

	public static List<Combination> findAllPossibleNumberCombinations(List<ValidCharacters> characterList,
			List<Integer> numberList) {
		// TODO: implement caching

		if (characterList.stream().filter(validChars -> validChars == ValidCharacters.DOT).count() != 0) {
			throw new IllegalArgumentException("Input must be separated into smaller parts");
		}

		int n = characterList.size();

		List<List<Integer>> combinationsForLength = CombinationGenerator.generateCombinations(n, numberList);

		StringGenerator stringGenerator = new StringGenerator(characterList);

		// check combinations and wrap them
		List<Combination> combinations = new ArrayList<>();

		// List<String> possibleCombinations = getAllCombinations(characterList);

		combinationsForLength.forEach(combination -> {
			Pattern pattern = compilePattern(combination);
			int sum = combination.stream().mapToInt(Integer::intValue).sum();
			int multiplier = (int) stringGenerator.getAllCombinationsWithNumberOfHashes(sum)
					.parallel()
					.filter(possibleCombination -> pattern.matcher(possibleCombination).matches())
					.count();
			if (multiplier > 0) {
				combinations.add(new Combination(combination, multiplier));
			}
		});

		return combinations;
	}

	public static Pattern compilePattern(List<Integer> numbers) {
		// creates a pattern from a list of numbers
		// for example, the list [1, 1, 3] will create the pattern
		// \.*#{1}\.+#{1}\.+#{3}\.*

		String pattern = "\\.*";

		for (int i = 0; i < numbers.size(); i++) {
			pattern += "#{" + numbers.get(i) + "}";

			if (i + 1 != numbers.size()) { // if not the last number
				pattern += "\\.+";
			}
		}

		pattern += "\\.*";

		return Pattern.compile(pattern);
	}

	public static List<String> getAllCombinations(List<ValidCharacters> characterList) {
		long questionMarks = characterList.stream()
				.filter(validChars -> validChars == ValidCharacters.QUESTION_MARK).count();
		long possibleCombinations = (long) Math.pow(2, questionMarks);

		List<String> combinations = new ArrayList<>();

		for (long i = 0; i < possibleCombinations; i++) {
			String combination = "";

			// convert the number to binary, the MSB is at index 0
			Boolean[] binary = new Boolean[(int) questionMarks];
			for (int j = 0; j < questionMarks; j++) {
				binary[j] = (i & (1 << j)) != 0;
			}

			int binaryIndex = 0;
			for (ValidCharacters character : characterList) {
				if (character == ValidCharacters.QUESTION_MARK) {
					combination += binary[binaryIndex] ? ValidCharacters.HASH.getCharacter()
							: ValidCharacters.DOT.getCharacter();
					binaryIndex++;
				} else {
					combination += character.getCharacter();
				}
			}

			combinations.add(combination);
		}

		return combinations;
	}

	enum ValidCharacters {
		HASH('#'),
		DOT('.'),
		QUESTION_MARK('?');

		private final char character;

		ValidCharacters(char character) {
			this.character = character;
		}

		public char getCharacter() {
			return character;
		}

		public static ValidCharacters fromChar(char character) {
			for (ValidCharacters validCharacter : ValidCharacters.values()) {
				if (validCharacter.getCharacter() == character) {
					return validCharacter;
				}
			}

			throw new IllegalArgumentException("Character " + character + " is not a valid character");
		}

		@Override
		public String toString() {
			return Character.toString(character);
		}
	}

	public class CombinationGenerator {
		public static List<List<Integer>> generateCombinations(int n, List<Integer> mustMatch) {
			// WARNING: for values above 30 this takes unreasonably long to compute)

			List<List<Integer>> combinations = new ArrayList<>();

			final List<Integer> nextMustMatch;
			if (mustMatch != null && mustMatch.size() > 1) {
				nextMustMatch = mustMatch.subList(1, mustMatch.size());
			} else {
				nextMustMatch = new ArrayList<>(List.of(0));
			}

			if (mustMatch.size() == 0) {
				return List.of(List.of());
			}

			IntStream.range(1, n + 1)
					.filter(number -> mustMatch == null || number == mustMatch.get(0))
					.forEach(number -> {
						int nextN = n - number - 1;
						List<List<Integer>> subCombinations = generateCombinations(nextN, nextMustMatch);
						for (List<Integer> subCombination : subCombinations) {
							// merge them together, so that [1] and [[],[2],[3,4]] becomes
							// [[1],[1,2],[1,3,4]]
							List<Integer> merged = Stream.concat(Stream.of(number),
									subCombination.stream())
									.toList();
							combinations.add(merged);
						}
					});

			combinations.add(List.of()); // add empty element

			return combinations;
		}
	}

	static class StringGenerator {
		private final List<ValidCharacters> characters;
		private final int numberOfHashes;
		private final int numberOfQuestionMarks;

		public StringGenerator(List<ValidCharacters> characters) {
			this.characters = characters;
			this.numberOfHashes = (int) characters.stream().filter(c -> c == ValidCharacters.HASH).count();
			this.numberOfQuestionMarks = (int) characters.stream().filter(c -> c == ValidCharacters.QUESTION_MARK)
					.count();
		}

		public String getStringFromBooleans(List<Boolean> questionMarkStates) {
			if (questionMarkStates.size() != numberOfQuestionMarks) {
				throw new IllegalArgumentException(
						"The size of the boolean list must match the number of question marks");
			}

			int questionMarkIndex = 0;
			StringBuilder stringBuilder = new StringBuilder();

			for (ValidCharacters character : characters) {
				if (character == ValidCharacters.HASH) {
					stringBuilder.append("#");
				} else if (character == ValidCharacters.QUESTION_MARK) {
					stringBuilder.append(questionMarkStates.get(questionMarkIndex) ? "#" : ".");
					questionMarkIndex++;
				}
			}

			return stringBuilder.toString();
		}

		public int getNumberOfHashes() {
			return numberOfHashes;
		}

		public int getNumberOfQuestionMarks() {
			return numberOfQuestionMarks;
		}

		static class BooleanArrayGenerator {

			public static Stream<List<Boolean>> generateBooleanArrays(int length, int numberOfTrues) {
				return generateCombinationsStream(new int[numberOfTrues], 0, 0, length);
			}

			// Helper method to generate combinations as a Stream
			private static Stream<List<Boolean>> generateCombinationsStream(int[] combination, int start, int depth,
					int n) {
				if (depth == combination.length) {
					return Stream.of(createList(n, combination.clone()));
				}

				return IntStream.range(start, n)
						.mapToObj(i -> {
							int[] newCombination = combination.clone();
							newCombination[depth] = i;
							return generateCombinationsStream(newCombination, i + 1, depth + 1, n);
						})
						.flatMap(s -> s);
			}

			// Converts a combination of indices to a list of Booleans
			private static List<Boolean> createList(int n, int[] indices) {
				List<Boolean> list = new ArrayList<>(n);
				for (int i = 0; i < n; i++) {
					list.add(false);
				}
				for (int index : indices) {
					list.set(index, true);
				}
				return list;
			}
		}

		public Stream<String> getAllCombinationsWithNumberOfHashes(int numberOfHashes) {
			int trues = numberOfHashes - this.numberOfHashes;

			if (trues < 0) {
				return Stream.of();
			}

			if (trues > this.numberOfQuestionMarks) {
				return Stream.of();
			}

			return BooleanArrayGenerator.generateBooleanArrays(this.numberOfQuestionMarks,
					trues).map(this::getStringFromBooleans);
		}
	}
}
