import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.LinkedHashMap;

class HASHMAPAlgorithm {
	private List<LinkedHashMap<String, Integer>> boxes = IntStream.range(0, 256)
			.mapToObj(i -> new LinkedHashMap<String, Integer>()).toList();

	public void process(String in) {
		String[] parts = in.split("[=-]");
		int hashValue = getHashCode(parts[0]);
		var map = boxes.get(hashValue);
		if (parts.length == 1) {
			if (map.containsKey(parts[0])) {
				map.remove(parts[0]);
			}
		} else {
			map.put(parts[0], Integer.parseInt(parts[1]));
		}
	}

	public void process(String[] in) {
		for (String s : in) {
			process(s);
		}
	}

	public static int getHashCode(String toHash) {
		AtomicInteger result = new AtomicInteger();
		toHash.chars().forEach(c -> {
			result.set(result.get() + c);
			result.set(result.get() * 17);
			result.set(result.get() % 256);
		});
		return result.get();
	}

	public long getFocusingPower() {
		long focusingPower = 0;
		for (int i = 0; i < 256; i++) {
			int boxMultiplier = i + 1;
			int slotMultiplier = 1;
			for (var entry : boxes.get(i).entrySet()) {
				focusingPower += boxMultiplier * slotMultiplier * entry.getValue();
				slotMultiplier++;
			}
		}
		return focusingPower;
	}
}

public class Day15 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "15", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (lines.size() != 1) {
			throw new RuntimeException("Expected 1 line of data, got " + lines.size());
		}

		String input = lines.get(0);

		String[] toHash = input.split("[,\n]");

		long task1 = Arrays.stream(toHash).mapToInt(HASHMAPAlgorithm::getHashCode).sum();
		System.out.println("Task 1: " + task1);

		HASHMAPAlgorithm algorithm = new HASHMAPAlgorithm();
		algorithm.process(toHash);
		long task2 = algorithm.getFocusingPower();
		System.out.println("Task 2: " + task2);
	}
}
