import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day6 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "06", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		long task1Answer = Paper.parseTask1(lines).getProductOfErrors();
		System.out.println("Task 1: " + task1Answer);

		long task2Answer = Paper.parseTask2(lines).getProductOfErrors();
		System.out.println("Task 2: " + task2Answer);
	}

	static class Paper {
		private record Roots(Double lower, Double upper) {
		}

		public static Roots findRoots(double a, double b, double c) {
			double discriminant = b * b - 4 * a * c;

			// Check if roots are real numbers
			if (discriminant < 0) {
				// No real roots
				return new Roots(null, null);
			}

			double sqrtDiscriminant = Math.sqrt(discriminant);
			double upper = (-b - sqrtDiscriminant) / (2 * a);
			double lower = (-b + sqrtDiscriminant) / (2 * a);

			return new Roots(lower, upper);
		}

		public record MaxValues(long time, long distance) {
			long findMaxError() {
				Roots roots = findRoots(-1, time, -distance);
				long lower = (long) Math.floor(roots.lower() + 1);
				long upper = (long) Math.ceil(roots.upper() - 1);
				return upper - lower + 1;
			}
		}

		private List<MaxValues> maxValues;

		public Paper(List<MaxValues> maxValues) {
			this.maxValues = maxValues;
		}

		public static Paper parseTask1(List<String> rawLines) {
			String[][] lines = getNumberPairs(rawLines);

			List<MaxValues> maxValues = IntStream.range(0, lines[0].length)
					.mapToObj(i -> new MaxValues(Long.parseLong(lines[0][i]), Long.parseLong(lines[1][i])))
					.toList();

			return new Paper(maxValues);
		}

		public static Paper parseTask2(List<String> rawLines) {
			String[][] lines = getNumberPairs(rawLines);

			long time = Long.parseLong(String.join("", lines[0]));
			long distance = Long.parseLong(String.join("", lines[1]));

			return new Paper(List.of(new MaxValues(time, distance)));
		}

		public static String[][] getNumberPairs(List<String> lines) {
			return lines.stream()
					.map(line -> line.split(":\\s+")[1].split("\\s+"))
					.toArray(String[][]::new);
		}

		public List<MaxValues> getMaxValues() {
			return maxValues;
		}

		public long getProductOfErrors() {
			return maxValues.stream()
					.mapToLong(MaxValues::findMaxError)
					.reduce(1, (a, b) -> a * b);
		}
	}
}