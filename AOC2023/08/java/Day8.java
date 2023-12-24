import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/* 
 * DISCLAIMER:
 * 
 * This doesn't work with random input data, but it is the commonly accepted solution and works with the specially crafted input data from AOC2023 Day 8.
 * 
 * Example of an input that would break this solution:
 * 
 * LLRL
 * 
 * AAA = (AAB, AAB)
 * AAB = (AAC, AAC)
 * AAC = (AAD, AAD)
 * AAD = (ZZZ, ZZZ)
 * ZZZ = (AAA, AAA)
 * BBA = (BBB, BBB)
 * BBB = (BBC, BBC)
 * BBC = (BBD, BBD)
 * BBD = (BBE, BBE)
 * BBE = (BBZ, BBZ)
 * BBZ = (BBA, BBA)
 */

// TODO: maybe take a look at Chinese Remainder Theorem

public class Day8 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "08", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		InstructionMap instructionMap = InstructionMap.parse(lines);
		System.out.println("Task 1: " + instructionMap.getDistanceFromTo("AAA", "ZZZ"));

		System.out.println("Task 2 LCM: " + instructionMap.getLCMGhostDistanceFromTo("A", "Z"));
		System.out.println("Task 2 Brute-force: " + instructionMap.getBruteForceGhostDistanceFromTo("A", "Z"));

	}

	static class InstructionMap {
		private Map<String, Node> nodes;
		private List<Direction> directions;

		public InstructionMap(List<Direction> directions, Map<String, Node> nodes) {
			this.directions = directions;
			this.nodes = nodes;
		}

		public static InstructionMap parse(List<String> lines) {
			List<Direction> directions = lines.get(0).chars()
					.mapToObj(c -> Direction.fromChar((char) c))
					.toList();

			List<String> nodeLines = lines.subList(2, lines.size());
			Map<String, Node> nodes = parseNodes(nodeLines);

			return new InstructionMap(directions, nodes);
		}

		private static Map<String, Node> parseNodes(List<String> lines) {
			Map<String, Node> map = new HashMap<>();
			for (String line : lines) {
				String[] parts = line.split("[^a-zA-Z]+"); // Split by any number of non-alphabetic characters
				map.put(parts[0], new Node(parts[1], parts[2]));
			}
			return map;
		}

		public long getDistanceFromTo(String start, String end) {
			long distance = 0;
			String currentNode = start;
			while (true) {
				for (Direction direction : directions) {
					if (currentNode.endsWith(end)) {
						return distance;
					}
					distance++;
					Node node = nodes.get(currentNode);
					String nextNode = (direction == Direction.LEFT ? node.left() : node.right());
					currentNode = nextNode;
				}
			}
		}

		public long getLCMGhostDistanceFromTo(String start, String end) {
			List<String> currentNodes = nodes.keySet().stream()
					.filter(node -> node.endsWith(start))
					.toList();
			return currentNodes.stream()
					.mapToLong(node -> getDistanceFromTo(node, end))
					.reduce(1L, Day8::lcm);
		}

		public long getBruteForceGhostDistanceFromTo(String start, String end) {
			List<String> currentNodes = nodes.keySet().stream()
					.filter(node -> node.endsWith(start))
					.toList();
			long iterator = 0;
			while (true) {
				for (Direction direction : directions) {
					if (currentNodes.stream().allMatch(node -> node.endsWith(end))) {
						return iterator;
					}

					iterator++;

					currentNodes = currentNodes.stream()
							.map(node -> nodes.get(node))
							.map(node -> (direction == Direction.LEFT ? node.left() : node.right()))
							.toList();
				}
			}
		}
	}

	static record Node(String left, String right) {
		public Node {
			if (left.length() != 3 || right.length() != 3) {
				throw new RuntimeException("String must be of length 3");
			}
		}
	}

	static enum Direction {
		LEFT('L'),
		RIGHT('R');

		private char character;

		private Direction(char character) {
			this.character = character;
		}

		public static Direction fromChar(char character) {
			for (Direction direction : Direction.values()) {
				if (direction.character == character) {
					return direction;
				}
			}
			throw new RuntimeException("Invalid direction character!");
		}
	}

	private static long lcm(long a, long b) {
		return Math.abs(a * b) / gcd(a, b);
	}

	private static long gcd(long a, long b) {
		if (b == 0) {
			return a;
		}
		return gcd(b, a % b);
	}
}
