import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

public class Day5 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "05", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToData))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String target = "location";

		Plan plan = new Plan(lines);
		List<Converter> converters = plan.getConverters();
		List<Resource> resources = plan.getResources();
		List<Resource> convertedResources = resources.stream().map(res -> {
			while (!res.getType().equals(target)) {
				converters.stream().filter(conv -> conv.getSource().equals(res.getType())).findFirst().get()
						.convert(res);
			}
			return res;
		}).toList();
		long min = convertedResources.stream().mapToLong(res -> res.getNumber()).min().getAsLong();
		System.out.println("Task 1: " + min);

	}

	static class Plan {
		private List<Resource> resources;
		private List<Converter> converters;

		private static final Pattern RESOURCE_PATTERN = Pattern
				.compile("(?<identifier>\\w+)s:\\s(?<everythingElse>.*)");

		public Plan(List<String> lines) {
			resources = parseResources(lines.get(0));

			converters = parseConverters(
					// discard the first line, then group the rest by empty lines
					String.join("\n", lines.subList(1, lines.size())).split("\n\n"));
		}

		private List<Resource> parseResources(String resourceString) {
			List<Resource> resources = new ArrayList<>();

			Matcher matcher = RESOURCE_PATTERN.matcher(resourceString);

			if (matcher.find()) {
				String startingRes = matcher.group("identifier");
				String[] numberStrings = matcher.group("everythingElse").split(" ");

				for (String numberString : numberStrings) {
					resources.add(new Resource(startingRes, Long.parseLong(numberString)));
				}
			}

			return resources;
		}

		private List<Converter> parseConverters(String[] converterStrings) {
			List<Converter> converters = new ArrayList<>();

			for (String converterString : converterStrings) {
				Converter converter = new Converter(converterString);
				converters.add(converter);
			}

			return converters;
		}

		public List<Converter> getConverters() {
			return converters;
		}

		public List<Resource> getResources() {
			return resources;
		}
	}

	static class Resource {
		String type;
		long number;

		public Resource(String type, long number) {
			this.type = type;
			this.number = number;
		}

		public String getType() {
			return type;
		}

		public long getNumber() {
			return number;
		}
	}

	static class Converter {
		String source;
		String destination;

		TreeMap<Long, Range> ranges;

		private static final Pattern FROM_TO_PATTERN = Pattern
				.compile("(?<source>\\w+)-to-(?<destination>\\w+) map:\n(?<numbers>.*)", Pattern.DOTALL);

		// I'm assuming that ranges can't overlap as that wouldn't rly make sense
		// TODO: input validation
		public Converter(String input) {
			ranges = new TreeMap<>();

			Matcher matcher = FROM_TO_PATTERN.matcher(input);

			matcher.find();

			this.source = matcher.group("source");
			this.destination = matcher.group("destination");

			String[] numberStrings = matcher.group("numbers").split("\n");

			List<Range> ranges = new ArrayList<>();

			for (String numberString : numberStrings) {
				ranges.add(new Range(numberString));
			}

			initMap(ranges);
		}

		public Converter(String source, String destination, List<Range> ranges) {
			this.source = source;
			this.destination = destination;
			this.ranges = new TreeMap<>();

			initMap(ranges);
		}

		private void initMap(List<Range> ranges) {
			this.ranges = new TreeMap<>();

			for (Range range : ranges) {
				this.ranges.put(range.getSourceStart(), range);
			}
		}

		public long convert(long sourceNumber) {
			var entry = ranges.floorEntry(sourceNumber);
			if (entry == null) {
				return sourceNumber;
			}
			return entry.getValue().map(sourceNumber);
		}

		public void convert(Resource resource) {
			resource.type = this.destination;
			resource.number = convert(resource.number);
		}

		public String getDestination() {
			return destination;
		}

		public String getSource() {
			return source;
		}

		public Collection<Range> getRanges() {
			return ranges.values();
		}
	}

	static class Range {
		private long sourceStart;
		private long destinationStart;
		private long range;

		public Range(String line) {
			String[] numbers = line.split(" ");
			this.destinationStart = Long.parseLong(numbers[0]);
			this.sourceStart = Long.parseLong(numbers[1]);
			this.range = Long.parseLong(numbers[2]);
		}

		public Range(long start, long stop, long offset) {
			this.sourceStart = start;
			this.destinationStart = start + offset;
			this.range = stop - start + 1;
		}

		public boolean isInRange(long source) {
			return (source >= sourceStart && source < sourceStart + range);
		}

		public long map(long source) {
			if (isInRange(source)) {
				return source + (destinationStart - sourceStart);
			}
			return source;
		}

		public long getSourceStart() {
			return sourceStart;
		}

		public long getDestinationStart() {
			return destinationStart;
		}

		public long getDestinationEnd() {
			return destinationStart + range - 1;
		}

		public long getSourceEnd() {
			return sourceStart + range - 1;
		}
	}
}
