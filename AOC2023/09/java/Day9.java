import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Day9 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "09", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		OASIS oasis = OASIS.parse(lines);
		System.out.println("Task 1: " + oasis.getNextValues().stream().mapToLong(Long::longValue).sum());
		System.out.println("Task 2: " + oasis.getPreviousValues().stream().mapToLong(Long::longValue).sum());
	}

	static class OASIS {

		// we could also precompute the diffs but I think that's overkill for such a
		// small input

		private List<List<Long>> values;

		public OASIS(List<List<Long>> values) {
			this.values = values;
		}

		public static OASIS parse(List<String> lines) {
			List<List<Long>> values = lines.stream()
					.map(line -> line.split(" "))
					.map(stringArr -> Arrays.stream(stringArr).map(Long::parseLong).toList())
					.toList();
			return new OASIS(values);
		}

		public List<Long> getNextValues() {
			return values.stream()
					.map(OASIS::getNextValue)
					.toList();
		}

		public static long getNextValue(List<Long> numbers) {
			if (numbers.stream().distinct().count() == 1) {
				return numbers.getLast(); // we could also return numbers.getFirst() for efficiency (for an ArrayList
											// it's always O(1), but for a LinkedList getFirst() is O(1) and getLast()
											// is O(n)) but I think it's more readable this way
			}

			return getNextValue(getDifferences(numbers)) + numbers.getLast();
		}

		public List<Long> getPreviousValues() {
			return values.stream()
					.map(OASIS::getPreviousValue)
					.toList();
		}

		public static long getPreviousValue(List<Long> numbers) {
			if (numbers.stream().distinct().count() == 1) {
				return numbers.getFirst();
			}

			return numbers.getFirst() - getPreviousValue(getDifferences(numbers));
		}

		public static List<Long> getDifferences(List<Long> numbers) {
			return IntStream.range(0, numbers.size() - 1)
					.mapToObj(index -> numbers.get(index + 1) - numbers.get(index))
					.toList();
		}
	}
}
