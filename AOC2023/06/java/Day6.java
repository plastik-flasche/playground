import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day6 {

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

	public static int findMaxError(int time, int distance) {
		Roots roots = findRoots(-1, time, -distance);

		int lower = (int) Math.floor(roots.lower() + 1);
		int upper = (int) Math.ceil(roots.upper() - 1);

		return upper - lower + 1;
	}

	private record MaxValues(int time, int distance) {
	}

	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "05", "DATA.txt").toAbsolutePath().normalize().toString();

		// List<String> lines = new ArrayList<>();

		// try {
		// lines = Files.readAllLines(Paths.get(pathToData));
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }

		/*
		 * Time: 62 73 75 65
		 * Distance: 644 1023 1240 1023
		 */

		MaxValues[] maxValues = new MaxValues[] {
				new MaxValues(62, 644),
				new MaxValues(73, 1023),
				new MaxValues(75, 1240),
				new MaxValues(65, 1023) };

		int[] errors = Arrays.stream(maxValues).mapToInt(maxValue -> findMaxError(maxValue.time(), maxValue.distance()))
				.toArray();

		Arrays.stream(errors).forEach(System.out::println);

		System.out.println(Arrays.stream(errors).reduce(1, (a, b) -> a * b));
	}
}