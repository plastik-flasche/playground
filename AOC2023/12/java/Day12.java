import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class Day12 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "12", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// I am aware that this is really slow, but it was quick to implement and
		// sufficient for task 1

		List<Integer> combinationNumbers = lines.stream().map(Day12::getCombinations).toList();
		System.out.println("Task 1: " + combinationNumbers.stream().mapToInt(Integer::intValue).sum());

		System.out.println("Time spent generating combinations: " + timeSpentGeneratingCombinations / 1000000 + "ms");
		System.out.println("Time spent matching combinations: " + timeSpentMatchingCombinations / 1000000 + "ms");
		System.out.println(
				"Total time: " + (timeSpentGeneratingCombinations + timeSpentMatchingCombinations) / 1000000 + "ms");
		System.out.println("Ratio: " + (double) timeSpentMatchingCombinations / timeSpentGeneratingCombinations);
	}

	static long timeSpentGeneratingCombinations = 0;
	static long timeSpentMatchingCombinations = 0;

	public static int getCombinations(String line) {
		String[] parts = line.split(" ");
		String firstVersion = parts[0];
		List<ValidCharacters> characterList = firstVersion.chars()
				.mapToObj(character -> ValidCharacters.fromChar((char) character))
				.toList();
		String[] numbers = parts[1].split(",");
		List<Integer> numbersList = Stream.of(numbers).map(Integer::parseInt).toList();

		Pattern pattern = compilePattern(numbersList);

		long startTime = System.nanoTime();
		List<String> combinations = getAllCombinations(characterList);
		timeSpentGeneratingCombinations += System.nanoTime() - startTime;

		startTime = System.nanoTime();
		int count = (int) combinations.stream()
				.filter(combination -> pattern.matcher(combination).matches())
				.count();
		timeSpentMatchingCombinations += System.nanoTime() - startTime;

		return count;
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

		System.out.println("Generating " + possibleCombinations + " combinations");

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
	}
}
