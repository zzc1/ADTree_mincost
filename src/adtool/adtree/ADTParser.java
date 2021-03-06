/* Generated By:ree&JavaCC: Do not edit this line. ADTParser.java */
package adtool.adtree;

import java.io.StringReader;
import java.io.Reader;

public class ADTParser implements ADTParserTreeConstants,
		ADTParserConstants {
	protected static ADTParserState ree = new ADTParserState();

	/**
	 * Parse a string
	 */
	public ADTParser(String s) {
		this((Reader) (new StringReader(s)));
	}

	/** Main entry point. */
	public static void main(String args[]) {
		System.out.println("Reading from standard input...");
		try {
			ADTNode n = ADTParser.parse();
			System.out.println(n.toString());
			System.out.println("Thank you.");
		} catch (Exception e) {
			System.out.println("Oops.");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/** Main production. */
	static final public ADTNode parse() throws ParseException {
		ADTNode root;
		switch ((ntk == -1) ? ntk() : ntk) {
		case OP:
		case AP:
		case CP:
		case IDENTIFIER:
			root = ADTPro();
			consume_token(0); {
			if (true)
				return root;
		}
			break;
		case 0:
			consume_token(0); {
			if (true)
				return null;
		}
			break;
		default:
			la1[0] = gen;
			consume_token(-1);
			throw new ParseException();
		}
		throw new Error("Missing return statement in function");
	}

	/** An ADTerm of opponent type. */
	static final public ADTNode ADTOpp() throws ParseException {
		/* @bgen(ree) ADTOpp */
		ADTNode n000 = new ADTNode(JJTADTOPP);
		boolean c000 = true;
		ree.openNodeScope(n000);
		Token t;
		try {
			switch ((ntk == -1) ? ntk() : ntk) {
			case IDENTIFIER:
				t = consume_token(IDENTIFIER);
				n000.setType(ADTNode.Type.LEAFO);
				n000.setName(t.image);
				break;
			case OO:
			case AO:
				switch ((ntk == -1) ? ntk() : ntk) {
				case OO:
					consume_token(OO);
					n000.setType(ADTNode.Type.OO);
					n000.setName("oo");
					break;
				case AO:
					consume_token(AO);
					n000.setType(ADTNode.Type.AO);
					n000.setName("ao");
					break;
				default:
					la1[1] = gen;
					consume_token(-1);
					throw new ParseException();
				}
				consume_token(LPAREN);
				ADTOpp();
				label_1: while (true) {
					switch ((ntk == -1) ? ntk() : ntk) {
					case COMMA:
						;
						break;
					default:
						la1[2] = gen;
						break label_1;
					}
					consume_token(COMMA);
					ADTOpp();
				}
				consume_token(RPAREN);
				break;
			case CO:
				consume_token(CO);
				consume_token(LPAREN);
				ADTOpp();
				consume_token(COMMA);
				ADTPro();
				consume_token(RPAREN);
				n000.setType(ADTNode.Type.CO);
				n000.setName("co");
				break;
			default:
				la1[3] = gen;
				consume_token(-1);
				throw new ParseException();
			}
			ree.closeNodeScope(n000, true);
			c000 = false;
			{
				if (true)
					return n000;
			}
		} catch (Throwable e000) {
			if (c000) {
				ree.clearNodeScope(n000);
				c000 = false;
			} else {
				ree.popNode();
			}
			if (e000 instanceof RuntimeException) {
				{
					if (true)
						throw (RuntimeException) e000;
				}
			}
			if (e000 instanceof ParseException) {
				{
					if (true)
						throw (ParseException) e000;
				}
			}
			{
				if (true)
					throw (Error) e000;
			}
		} finally {
			if (c000) {
				ree.closeNodeScope(n000, true);
			}
		}
		throw new Error("Missing return statement in function");
	}

	/** An ADTerm of proponent type. */
	static final public ADTNode ADTPro() throws ParseException {
		ADTNode n000 = new ADTNode(JJTADTPRO);
		boolean c000 = true;
		ree.openNodeScope(n000);
		Token t;
		try {
			switch ((ntk == -1) ? ntk() : ntk) {
			case IDENTIFIER:
				t = consume_token(IDENTIFIER);
				n000.setType(ADTNode.Type.LEAFP);
				n000.setName(t.image);
				break;
			case OP:
			case AP:
				switch ((ntk == -1) ? ntk() : ntk) {
				case OP:
					consume_token(OP);
					n000.setType(ADTNode.Type.OP);
					n000.setName("op");
					break;
				case AP:
					consume_token(AP);
					n000.setType(ADTNode.Type.AP);
					n000.setName("ap");
					break;
				default:
					la1[4] = gen;
					consume_token(-1);
					throw new ParseException();
				}
				consume_token(LPAREN);
				ADTPro();
				label_2: while (true) {
					switch ((ntk == -1) ? ntk() : ntk) {
					case COMMA:
						;
						break;
					default:
						la1[5] = gen;
						break label_2;
					}
					consume_token(COMMA);
					ADTPro();
				}
				consume_token(RPAREN);
				break;
			case CP:
				consume_token(CP);
				consume_token(LPAREN);
				ADTPro();
				consume_token(COMMA);
				ADTOpp();
				consume_token(RPAREN);
				n000.setType(ADTNode.Type.CP);
				n000.setName("cp");
				break;
			default:
				la1[6] = gen;
				consume_token(-1);
				throw new ParseException();
			}
			ree.closeNodeScope(n000, true);
			c000 = false;
			{
				if (true)
					return n000;
			}
		} catch (Throwable e000) {
			if (c000) {
				ree.clearNodeScope(n000);
				c000 = false;
			} else {
				ree.popNode();
			}
			if (e000 instanceof RuntimeException) {
				{
					if (true)
						throw (RuntimeException) e000;
				}
			}
			if (e000 instanceof ParseException) {
				{
					if (true)
						throw (ParseException) e000;
				}
			}
			{
				if (true)
					throw (Error) e000;
			}
		} finally {
			if (c000) {
				ree.closeNodeScope(n000, true);
			}
		}
		throw new Error("Missing return statement in function");
	}

	static private boolean initialized_once = false;
	/** Generated Token Manager. */
	static public ADTParserTokenManager token_source;
	static SimpleCharStream input_stream;
	/** Current token. */
	static public Token token;
	/** Next token. */
	static public Token nt;
	static private int ntk;
	static private int gen;
	static final private int[] la1 = new int[7];
	static private int[] la1_0;
	static {
		la1_init_0();
	}

	private static void la1_init_0() {
		la1_0 = new int[] { 0x4581, 0x60, 0x800, 0x4260, 0x180, 0x800, 0x4580, };
	}

	/** Constructor with InputStream. */
	public ADTParser(java.io.InputStream stream) {
		this(stream, null);
	}

	/** Constructor with InputStream and supplied encoding */
	public ADTParser(java.io.InputStream stream, String encoding) {
		if (initialized_once) {
			System.out.println("ERROR: Second call to constructor of static parser.  ");
			System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
			System.out.println("       during parser generation.");
			throw new Error();
		}
		initialized_once = true;
		try {
			input_stream = new SimpleCharStream(stream, encoding, 1, 1);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		token_source = new ADTParserTokenManager(input_stream);
		token = new Token();
		ntk = -1;
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	/** Reinitialise. */
	static public void ReInit(java.io.InputStream stream) {
		ReInit(stream, null);
	}

	/** Reinitialise. */
	static public void ReInit(java.io.InputStream stream, String encoding) {
		try {
			input_stream.ReInit(stream, encoding, 1, 1);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		token_source.ReInit(input_stream);
		token = new Token();
		ntk = -1;
		ree.reset();
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	/** Constructor. */
	public ADTParser(Reader stream) {
		if (initialized_once) {
			System.out.println("ERROR: Second call to constructor of static parser. ");
			System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
			System.out.println("       during parser generation.");
			throw new Error();
		}
		initialized_once = true;
		input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new ADTParserTokenManager(input_stream);
		token = new Token();
		ntk = -1;
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	/** Reinitialise. */
	static public void ReInit(Reader stream) {
		input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(input_stream);
		token = new Token();
		ntk = -1;
		ree.reset();
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	/** Constructor with generated Token Manager. */
	public ADTParser(ADTParserTokenManager tm) {
		if (initialized_once) {
			System.out.println("ERROR: Second call to constructor of static parser. ");
			System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
			System.out.println("       during parser generation.");
			throw new Error();
		}
		initialized_once = true;
		token_source = tm;
		token = new Token();
		ntk = -1;
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	/** Reinitialise. */
	public void ReInit(ADTParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		ntk = -1;
		ree.reset();
		gen = 0;
		for (int i = 0; i < 7; i++)
			la1[i] = -1;
	}

	static private Token consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		ntk = -1;
		if (token.kind == kind) {
			gen++;
			return token;
		}
		token = oldToken;
		kind = kind;
		throw generateParseException();
	}

	/** Get the next Token. */
	static final public Token getNextToken() {
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		ntk = -1;
		gen++;
		return token;
	}

	/** Get the specific Token. */
	static final public Token getToken(int index) {
		Token t = token;
		for (int i = 0; i < index; i++) {
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	static private int ntk() {
		if ((nt = token.next) == null)
			return (ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (ntk = nt.kind);
	}

	static private java.util.List<int[]> expentries = new java.util.ArrayList<int[]>();
	static private int[] expentry;
	static private int kind = -1;

	/** Generate ParseException. */
	static public ParseException generateParseException() {
		expentries.clear();
		boolean[] la1tokens = new boolean[20];
		if (kind >= 0) {
			la1tokens[kind] = true;
			kind = -1;
		}
		for (int i = 0; i < 7; i++) {
			if (la1[i] == gen) {
				for (int j = 0; j < 32; j++) {
					if ((la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 20; i++) {
			if (la1tokens[i]) {
				expentry = new int[1];
				expentry[0] = i;
				expentries.add(expentry);
			}
		}
		int[][] exptokseq = new int[expentries.size()][];
		for (int i = 0; i < expentries.size(); i++) {
			exptokseq[i] = expentries.get(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	/** Enable tracing. */
	static final public void enable_tracing() {
	}

	/** Disable tracing. */
	static final public void disable_tracing() {
	}

}
