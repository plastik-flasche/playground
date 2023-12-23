import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Comparator;

public class Day7 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "07", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		SetOfHands task1 = SetOfHands.parseTask1(lines);
		System.out.println("Task 1: " + task1.getWinnings());

		SetOfHands task2 = SetOfHands.parseTask2(lines);
		System.out.println("Task 2: " + task2.getWinnings());
	}

	static class SetOfHands {
		List<Hand> hands;

		public SetOfHands(List<Hand> hands) {
			this.hands = hands.stream()
					.sorted()
					.toList();
		}

		public static SetOfHands parseTask1(List<String> lines) {
			List<Hand> hands = lines.stream()
					.map(line -> new Hand(line, true))
					.toList();

			return new SetOfHands(hands);
		}

		public static SetOfHands parseTask2(List<String> lines) {
			List<Hand> hands = lines.stream()
					.map(line -> new Hand(line, false))
					.toList();

			return new SetOfHands(hands);
		}

		public long getWinnings() {
			return IntStream.range(0, hands.size())
					.map(i -> hands.get(i).calculateWinnings(i + 1))
					.sum();
		}
	}

	static class Hand implements Comparable<Hand> {
		private List<Card> cards;
		private int bid;
		private HandType type;

		public Hand(String input, boolean isTask1) {
			String[] components = input.split(" ");

			if (components[0].length() != 5)
				throw new IllegalArgumentException("Hand must be 5 cards long");

			this.cards = components[0].chars()
					.mapToObj(character -> Card.fromChar((char) character, isTask1))
					.toList();

			this.bid = Integer.parseInt(components[1]);

			this.type = HandType.calculate(cards);
		}

		public List<Card> getCards() {
			return this.cards;
		}

		public int calculateWinnings(int ranking) {
			return bid * ranking;
		}

		@Override
		public int compareTo(Hand other) {
			int typeComparison = this.type.compareTo(other.type);
			if (typeComparison != 0) {
				return typeComparison;
			}
			for (int i = 0; i < this.cards.size(); i++) {
				int comparison = this.cards.get(i).compareTo(other.cards.get(i));
				if (comparison != 0) {
					return comparison;
				}
			}

			return 0;
		}

		@Override
		public String toString() {
			return String.format("%3d ", bid) + type.name() + ": "
					+ String.join(", ", cards.stream().map(card -> card.name()).toArray(String[]::new));
		}
	}

	private enum HandType {
		HIGH_CARD,
		ONE_PAIR,
		TWO_PAIR,
		THREE_OF_A_KIND,
		FULL_HOUSE,
		FOUR_OF_A_KIND,
		FIVE_OF_A_KIND;

		public static HandType calculate(List<Card> cards) {
			AtomicInteger jokerAmount = new AtomicInteger(0);
			List<Long> regularity = cards.stream()
					.collect(Collectors.groupingBy(card -> card, Collectors.counting()))
					.entrySet().stream()
					.map(entry -> {
						if (entry.getKey() == Card.JOKER) {
							jokerAmount.set(entry.getValue().intValue());
							return 0L;
						}
						return entry.getValue();
					})
					.sorted(Comparator.reverseOrder())
					.toList();

			int mostCommon = regularity.get(0).intValue() + jokerAmount.get();
			int secondMostCommon = 0;
			if (regularity.size() > 1) {
				secondMostCommon = regularity.get(1).intValue();
			}

			return switch (mostCommon) {
				case 1 -> HIGH_CARD;
				case 2 -> {
					if (secondMostCommon == 2) {
						yield TWO_PAIR;
					}
					yield ONE_PAIR;
				}
				case 3 -> {
					if (secondMostCommon == 2) {
						yield FULL_HOUSE;
					}
					yield THREE_OF_A_KIND;
				}
				case 4 -> FOUR_OF_A_KIND;
				case 5 -> FIVE_OF_A_KIND;
				default -> throw new RuntimeException("More than 5 values in list");
			};
		}
	}

	private enum Card {
		JOKER('-'),
		TWO('2'),
		THREE('3'),
		FOUR('4'),
		FIVE('5'),
		SIX('6'),
		SEVEN('7'),
		EIGHT('8'),
		NINE('9'),
		TEN('T'),
		JACK('J'),
		QUEEN('Q'),
		KING('K'),
		ACE('A');

		private final char character;

		Card(char character) {
			this.character = character;
		}

		public static Card fromChar(char character, boolean isTask1) {
			if (!isTask1 && character == 'J')
				return JOKER;
			for (Card card : Card.values()) {
				if (card.character == character) {
					return card;
				}
			}
			throw new RuntimeException("Invalid card character!");
		}
	}
}
