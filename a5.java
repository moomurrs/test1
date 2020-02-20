import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

class LineWrap {
	/**
	 * A hash table for use with the top down dynamic programming solution
	 */
	static Map<Pair<List<String>, Integer>, Pair<String, Integer>> hash = new HashMap<>();
	private int lineWidth;

	//-------------------------------------------------------------------------

	LineWrap(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * A simple way of running the greedy algorithm
	 */
	static String runGreedy(String s, int lineWidth) throws NoSuchElementE {
		List<String> words = List.fromArray(s.split("\\s"));
		LineWrap wrap = new LineWrap(lineWidth);
		String w = words.getFirst();
		String para = w + wrap.greedy(words.getRest(), lineWidth - w.length());
		return para;
	}

	//-------------------------------------------------------------------------

	/**
	 * A simple way of running the dynamic programming algorithm
	 */
	static String runDP(String s, int lineWidth) throws NoSuchElementE {
		hash = new WeakHashMap<>();
		List<String> words = List.fromArray(s.split("\\s"));
		LineWrap wrap = new LineWrap(lineWidth);
		String w = words.getFirst();
		Pair<String, Integer> sub = wrap.dpTD(words.getRest(), lineWidth - w.length());
		String para = w + sub.getFst();
		return para;
	}

	/**
	 * Here you are to implement the dynamic programming algorithm in a bottom-up fashion.
	 * You should still use a hash table as shown below but its management is done
	 * "manually" not implicitly when entering / exiting recursive calls.
	 */
	static String dpBU(String s, int lineWidth) {
		Map<Pair<Integer, Integer>, Pair<String, Integer>> hash = new HashMap<>();



		return null;
	}

	/**
	 * The greedy implementation keeps consuming words from the given list
	 * of words while keeping track of how much space is left on the current line.
	 * If the current word with a preceding space would fit on the current line, the
	 * algorithm continues with the remaining words and an adjusted space.
	 * If the word preceded by a space does not fit on the current line, a new line
	 * is inserted instead and the algorithm continues with the rest of the words and
	 * an adjusted space.
	 */
	String greedy(List<String> words, int space) {
		//TODO redesign greedy to use try catch
		try {
			// base case to end recursion
			if (words.length() == 0) {
				return "";
			}

			String w = words.getFirst();

			// try to insert the first word on the same line. if not, make a new line and reset the space
			if (w.length() <= space) {
				// always added a space before inserting the first word.
				// make recursive call on rest
				return " " + w + greedy(words.getRest(), space - 1 - w.length());

			} else {
				// if first can't fit, make a new line and strip the first space that comes from recursive calls
				return "\n" + greedy(words, this.lineWidth).substring(1);
			}

		} catch (NoSuchElementE e) {
			e.printStackTrace();
		}

		return "ERROR";

	}

	//-------------------------------------------------------------------------

	/**
	 * The greedy algorithm always adds words to the current line as long as they
	 * would fit. The dynamic programming algorithm instead considers two options
	 * for each word: add it to the current line, or insert a new line before
	 * the word. For each possibility, an estimate of "badness" is calculated
	 * and the best option is chosen. The "badness" of a solution is the sum
	 * of the cubes of the amount of space left on each line (except the last line).
	 * For example, if the line width is 6 and we have the following text:
	 * <p>
	 * 1234
	 * 12 45
	 * 123
	 * 12
	 * <p>
	 * then we calculate the badness as follows: the first line has 2 unused spaces
	 * at the end, the next line has 1, and the third has 3. The final line is perfect
	 * by definition. So the "badness" estimate is:
	 * 2^3 + 1^3 + 3^3 = 8 + 1 + 27 = 36
	 * <p>
	 * In contrast if the text was:
	 * <p>
	 * 1234
	 * 12 45
	 * 123 12
	 * <p>
	 * the "badness" would be: 2^3 + 1^3 = 8 + 1 = 9
	 * <p>
	 * so we prefer the second arrangement.
	 * <p>
	 * I strongly suggest you first write a plain recursive solution and test it on the small
	 * example (test1). Once that words properly, you can add the hash table management to
	 * get your final dynamic programming solution. Without the hash table, the algorithm
	 * will really not work on even a moderate size paragraph.
	 */

	//NOTE: Pair<String, Integer> is an object which keeps track of a running String with its corresponding "badness"

	Pair<String, Integer> dpTD(List<String> words, int space) {

		return hash.computeIfAbsent(new Pair<>(words, space), p ->{

			try {

				int remainingTake = space - 1 - words.getFirst().length();
				int remainingSkip = this.lineWidth - words.getFirst().length();
				if (remainingTake >= 0) {
					// run this branch if we can take a word on the same line
					// then ask what are the possibilities for taking and skipping


					// take the first word to current line
					Pair<String, Integer> tempTake = dpTD(words.getRest(), remainingTake);
					Pair<String, Integer> pTake = new Pair<>(" " + words.getFirst() + tempTake.getFst(), tempTake.getSnd());


					// skip the first word to a new line
					Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
					Pair<String, Integer> pSkip = new Pair<>("\n" + words.getFirst() + tempSkip.getFst(), (int)Math.pow(space, 3) + tempSkip.getSnd());


					if (pTake.getSnd() < pSkip.getSnd()) {
						// pTake has a lower badness
						return pTake;
					} else if (pSkip.getSnd() < pTake.getSnd()){
						// pSkip has a lower badness
						return pSkip;
					}else{
						// they are equal. Favor newline
						return pSkip;
					}
				} else {
					// run this branch if we cannot add to current line, only skip to new line
					// create an object to house the result from recursion from skip
					// because there's no space, calculate the badness of the current line also
					Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
					Pair<String, Integer> pSkip = new Pair<>("\n" + words.getFirst() + tempSkip.getFst(), (int)Math.pow(space, 3) + tempSkip.getSnd());
					return pSkip;
				}
			} catch (NoSuchElementE e) {
				// base case
				// non-existent element accessed, return a blank pair
				return new Pair<>("", 0);
			}

		});

		/*
		//TODO fails test5, Buzz pushed to newline
		//TODO fails test7, items not pushed to newline
		return hash.computeIfAbsent(new Pair<>(words, space), p ->{

			try {

				int remainingTake = space - 1 - words.getFirst().length();
				if (remainingTake >= 0) {
					// run this branch if we can take a word on the same line
					// then ask what are the possibilities for taking and skipping


					// take the first word
					Pair<String, Integer> tempTake = dpTD(words.getRest(), remainingTake);
					Pair<String, Integer> pTake = new Pair<>(" " + words.getFirst() + tempTake.getFst(), remainingTake);


					// skip the first word and add new line
					int remainingSkip = this.lineWidth - words.getFirst().length();
					Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
					Pair<String, Integer> pSkip = new Pair<>("\n" + words.getFirst() + tempSkip.getFst(), remainingSkip);
					int badnessTake = (int)Math.pow(pTake.getSnd(), 3);
					int badnessSkip = (int)Math.pow(pSkip.getSnd(), 3);
					if (badnessTake < badnessSkip) {
						// pTake has a lower badness
						return pTake;
					} else if (badnessSkip < badnessTake){
						// pSkip has a lower badness
						return pSkip;
					}else{
						// they are equal. Favor newline
						return pSkip;
					}
				} else {
					// run this branch if we cannot add to current line, only skip to new line
					// create an object to house the result from recursion from skip
					int remainingSkip = this.lineWidth - words.getFirst().length();
					Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
					Pair<String, Integer> pSkip = new Pair<>("\n" + words.getFirst() + tempSkip.getFst(), remainingSkip);
					return pSkip;
				}
			} catch (NoSuchElementE e) {
				// base case
				// non-existent element accessed, return a blank pair
				return new Pair<>("", 0);
			}

		});

		 */



		/*
		//working dpTD without hashmap
		try {

			int remainingTake = space - 1 - words.getFirst().length();
			if (remainingTake > 0) {
				// run this branch if we can take a word on the same line
				// then ask what are the possibilities for taking and skipping
				Pair<String, Integer> current = new Pair<>(words.getFirst(), remainingTake);
				// take the first word
				Pair<String, Integer> tempTake = dpTD(words.getRest(), remainingTake);
				Pair<String, Integer> pTake = new Pair<>(" " + current.getFst() + tempTake.getFst(), remainingTake);
				//TODO what is key and value?
				//TODO when to hash?

				// skip the first word and add new line
				int remainingSkip = this.lineWidth - words.getFirst().length();
				Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
				Pair<String, Integer> pSkip = new Pair<>("\n" + current.getFst() + tempSkip.getFst(), remainingSkip);

				if ((int) Math.pow(pTake.getSnd(), 3) < (int) Math.pow(pSkip.getSnd(), 3)) {
                    // pTake has a lower badness
                    // to determine hash value, look at the definition of hash above
                    // we hash the inputs, words and space, as key of type Pair then store value as the ideal Pair badness
                    hash.put(new Pair<>(words, space), pTake)
                    return pTake;
                } else {
                    // pSkip has a lower badness
                    return pSkip;
                }
            } else {
                // run this branch if we cannot add to current line, only skip to new line
                // create an object to house the result from recursion from skip
                int remainingSkip = this.lineWidth - words.getFirst().length();
                Pair<String, Integer> current = new Pair<>(words.getFirst(), remainingSkip);
                Pair<String, Integer> tempSkip = dpTD(words.getRest(), remainingSkip);
                Pair<String, Integer> pSkip = new Pair<>("\n" + current.getFst() + tempSkip.getFst(), remainingSkip);
                return pSkip;
            }
        } catch (NoSuchElementE e) {
            // base case
            // non-existent element accessed, return a blank pair
            return new Pair<>("", 0);
        }

		 */



	}
}
