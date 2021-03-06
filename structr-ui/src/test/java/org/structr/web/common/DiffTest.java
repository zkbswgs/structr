package org.structr.web.common;

import java.util.List;
import org.structr.core.graph.Tx;
import org.structr.web.Importer;
import org.structr.web.diff.InvertibleModificationOperation;
import org.structr.web.entity.dom.Page;

/**
 *
 * @author Christian Morgner
 */
public class DiffTest extends StructrUiTest {

	public void testReplaceContent() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {
				return from.replace("Test", "Wurst");
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>Wurst</body>\n" +
			"</html>",
			result1
		);
	}

	public void testInsertHeading() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {
				return from.replace("Test", "<h1>Title text</h1>");
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <h1>Title text</h1>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testInsertDivBranch() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {
				return from.replace("Test", "<div><h1>Title text</h1></div>");
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <h1>Title text</h1>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testInsertDivBranch2() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {
				return from.replace("Test", "<div><div><h1>Title text</h1><p>paragraph</p></div></div>");
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <div>\n" +
			"        <h1>Title text</h1>\n" +
			"        <p>paragraph</p>\n" +
			"      </div>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testInsertMultipleTextNodes() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {
				return from.replace("Test", "Test<b>bold</b>between<i>italic</i>Text");
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>Test<b>bold</b>between<i>italic</i>Text</body>\n" +
			"</html>",
			result1
		);
	}

	public void testModifyMultipleTextNodes2() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body>Test<b>bold</b>between<i>italic</i>Text</body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				String mod = from;

				mod = mod.replace("bold", "BOLD");
				mod = mod.replace("between", "BETWEEN");
				mod = mod.replace("italic", "ITALIC");
				mod = mod.replace("Text", "abcdef");

				return mod;
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>Test<b>BOLD</b>BETWEEN<i>ITALIC</i>abcdef</body>\n" +
			"</html>",
			result1
		);
	}

	public void testReparentOneLevel() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>Title text</h1></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int startPos = buf.indexOf("<h1");
				int endPos   = buf.indexOf("</h1>") + 5;

				// insert from back to front, otherwise insert position changes
				buf.insert(endPos, "</div>");
				buf.insert(startPos, "<div>");

				return buf.toString();
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <h1>Title text</h1>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testReparentTwoLevels() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>Title text</h1></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int startPos = buf.indexOf("<h1");
				int endPos   = buf.indexOf("</h1>") + 5;

				// insert from back to front, otherwise insert position changes
				buf.insert(endPos, "</div></div>");
				buf.insert(startPos, "<div><div>");

				return buf.toString();
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <div>\n" +
			"        <h1>Title text</h1>\n" +
			"      </div>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testReparentThreeLevels() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>Title text</h1></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int startPos = buf.indexOf("<h1");
				int endPos   = buf.indexOf("</h1>") + 5;

				// insert from back to front, otherwise insert position changes
				buf.insert(endPos, "</div></div></div>");
				buf.insert(startPos, "<div><div><div>");

				return buf.toString();
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <div>\n" +
			"        <div>\n" +
			"          <h1>Title text</h1>\n" +
			"        </div>\n" +
			"      </div>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

	public void testMove() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>Title text</h1><div><h2>subtitle</h2></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int startPos = buf.indexOf("<h1");
				int endPos   = buf.indexOf("</h1>") + 5;

				// cut out <h1> block
				final String toMove = buf.substring(startPos, endPos);
				buf.replace(startPos, endPos, "");

				// insert after <h2>
				int insertPos = buf.indexOf("</h2>") + 5;

				// insert from back to front, otherwise insert position changes
				buf.insert(insertPos, toMove);

				return buf.toString();
			}
		});


		assertEquals(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n" +
			"    <div>\n" +
			"      <h2>subtitle</h2>\n" +
			"      <h1>Title text</h1>\n" +
			"    </div>\n" +
			"  </body>\n" +
			"</html>",
			result1
		);
	}

// TODO: Instable test, needs review and fix!!
//	public void testCutAndPaste() {
//
//		final StringBuilder clipboard = new StringBuilder();
//
//		final String result1 = testDiff("<html><head><title>Title</title></head><body><div><h2>one</h2></div><div><h2>two</h2></div><div><h2>three</h2></div><div><h2>four</h2></div></body></html>", new org.neo4j.helpers.Function<String, String>() {
//
//			@Override
//			public String apply(String from) {
//
//				final StringBuilder buf = new StringBuilder(from);
//
//				// cut the first two div/h2 blocks and store them for later
//				for (int i=0; i<2; i++) {
//
//					int startPos = buf.indexOf("<div");
//					int endPos   = buf.indexOf("</div>") + 6;
//
//					// cut out <h1> block
//					clipboard.append(buf.substring(startPos, endPos));
//					buf.replace(startPos, endPos, "");
//				}
//
//				return buf.toString();
//			}
//		});
//
//		assertEquals(
//			"<!DOCTYPE html>\n" +
//			"<html>\n" +
//			"  <head>\n" +
//			"    <title>Title</title>\n" +
//			"  </head>\n" +
//			"  <body>\n" +
//			"    <div>\n" +
//			"      <h2>three</h2>\n" +
//			"    </div>\n" +
//			"    <div>\n" +
//			"      <h2>four</h2>\n" +
//			"    </div>\n" +
//			"  </body>\n" +
//			"</html>",
//			result1
//		);
//
//		// remove data-hash=... from clipboard buffer
//		int pos = -1;
//		do {
//			pos = clipboard.indexOf(" data-hash");
//			if (pos != -1) {
//
//				clipboard.replace(pos, pos+21, "");
//			}
//
//
//		} while (pos != -1);
//
//		final String result2 = testDiff(result1, new org.neo4j.helpers.Function<String, String>() {
//
//			@Override
//			public String apply(String from) {
//
//				final StringBuilder buf = new StringBuilder(from);
//
//				final int insertPos = buf.indexOf("<div");
//				buf.insert(insertPos, clipboard.toString());
//
//				return buf.toString();
//			}
//		});
//
//		assertEquals(
//			"<!DOCTYPE html>\n" +
//			"<html>\n" +
//			"  <head>\n" +
//			"    <title>Title</title>\n" +
//			"  </head>\n" +
//			"  <body>\n" +
//			"    <div>\n" +
//			"      <h2>one</h2>\n" +
//			"    </div>\n" +
//			"    <div>\n" +
//			"      <h2>two</h2>\n" +
//			"    </div>\n" +
//			"    <div>\n" +
//			"      <h2>three</h2>\n" +
//			"    </div>\n" +
//			"    <div>\n" +
//			"      <h2>four</h2>\n" +
//			"    </div>\n" +
//			"  </body>\n" +
//			"</html>",
//			result2
//		);
//	}

	public void testSwap() {

		final StringBuilder clipboard = new StringBuilder();

		final String result1 = testDiff("<html><head><title>Title</title></head><body><div><h2>one</h2></div><div><h2>two</h2></div><div><h2>three</h2></div><div><h2>four</h2></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int cutStart = buf.indexOf("<h2") - 33;
				int cutEnd = buf.indexOf("</h2>") + 21;

				// cut out <h1> block
				clipboard.append(buf.substring(cutStart, cutEnd));
				buf.replace(cutStart, cutEnd, "");

				int insert = buf.indexOf("</h2>") + 20;

				buf.insert(insert, clipboard.toString());

				return buf.toString();
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <div>\n"
			+ "      <h2>two</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>one</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>three</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>four</h2>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testAddAttributes() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><div><h2>one</h2></div><div><h2>two</h2></div><div><h2>three</h2></div><div><h2>four</h2></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int insert = buf.indexOf("<div ") + 5;

				buf.insert(insert, "class='test' id='one'");

				return buf.toString();
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <div class=\"test\" id=\"one\">\n"
			+ "      <h2>one</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>two</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>three</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>four</h2>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testModifyRemoveAttributes() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><div><h2 class=\"test\" id=\"one\">one</h2></div><div><h2>two</h2></div><div><h2 id=\"three\">three</h2></div><div><h2>four</h2></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				String modified = from;

				modified = modified.replace(" class=\"test\"", " class=\"foo\"");
				modified = modified.replace(" id=\"one\"", " id=\"two\"");
				modified = modified.replace(" id=\"three\"", " class=\"test\"");

				return modified;
			}
		});

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <div>\n"
			+ "      <h2 class=\"foo\" id=\"two\">one</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>two</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2 class=\"test\">three</h2>\n"
			+ "    </div>\n"
			+ "    <div>\n"
			+ "      <h2>four</h2>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testBlockMoveUp() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><div>Text<h2>one</h2><p>two</p></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder clipboard = new StringBuilder();
				final StringBuilder buf = new StringBuilder(from);

				int cutStart = buf.indexOf("<h2");
				int cutEnd = buf.indexOf("</p>") + 9;

				// cut out <h1> block
				clipboard.append(buf.substring(cutStart, cutEnd));
				buf.replace(cutStart, cutEnd, "");

				int insert = buf.indexOf("<div");
				buf.insert(insert, clipboard.toString());

				return buf.toString();
			}
		});

		System.out.println(result1);

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <h2>one</h2>\n"
			+ "    <p>two</p>\n"
			+ "    <div>Text </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testBlockMoveDown() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><div>Text<h2>one</h2><div><p>two</p></div></div></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder clipboard = new StringBuilder();
				final StringBuilder buf = new StringBuilder(from);

				int cutStart = buf.indexOf("<h2");
				int cutEnd = buf.indexOf("</h2>") + 5;

				// cut out <h1> block
				clipboard.append(buf.substring(cutStart, cutEnd));
				buf.replace(cutStart, cutEnd, "");

				int insert = buf.indexOf("<p ");

				buf.insert(insert, clipboard.toString());

				System.out.println(buf.toString());

				return buf.toString();
			}
		});

		System.out.println(result1);

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <div>Text \n"
			+ "      <div>\n"
			+ "        <h2>one</h2>\n"
			+ "        <p>two</p>\n"
			+ "      </div>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testSurroundBlock() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>title</h1><p>text</p></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				final StringBuilder buf = new StringBuilder(from);

				int insertStart = buf.indexOf("<h1");
				int insertEnd = buf.indexOf("</p>") + 4;

				buf.insert(insertEnd, "</div>");
				buf.insert(insertStart, "<div>");

				return buf.toString();
			}
		});

		System.out.println(result1);

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <div>\n"
			+ "      <h1>title</h1>\n"
			+ "      <p>text</p>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}

	public void testModifyTag() {

		final String result1 = testDiff("<html><head><title>Title</title></head><body><h1>title</h1><p>text</p></body></html>", new org.neo4j.helpers.Function<String, String>() {

			@Override
			public String apply(String from) {

				String modified = from;

				modified = modified.replace("<h1 ", "<h2 ");
				modified = modified.replace("</h1>", "</h2>");

				modified = modified.replace("<p ", "<a ");
				modified = modified.replace("</p>", "</a>");

				return modified;
			}
		});

		System.out.println(result1);

		assertEquals(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>Title</title>\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <h2>title</h2>\n"
			+ "    <a>text</a>\n"
			+ "  </body>\n"
			+ "</html>",
			result1
		);
	}









	private String testDiff(final String source, final org.neo4j.helpers.Function<String, String> modifier) {

		StringBuilder buf = new StringBuilder();

		try (final Tx tx = app.tx()) {

			final Page sourcePage     = Importer.parsePageFromSource(securityContext, source, "test");
			final String sourceHtml   = sourcePage.getContent(RenderContext.EditMode.RAW);
			final String modifiedHtml = modifier.apply(sourceHtml);

			// parse page from modified source
			final Page modifiedPage = Importer.parsePageFromSource(securityContext, modifiedHtml, "Test");

			final List<InvertibleModificationOperation> changeSet = Importer.diffPages(sourcePage, modifiedPage);

			for (final InvertibleModificationOperation op : changeSet) {

				System.out.println(op);

				// execute operation
				op.apply(app, sourcePage, modifiedPage);

				System.out.println("############################################################################################");
				System.out.println(sourcePage.getContent(RenderContext.EditMode.NONE));
			}

			buf.append(sourcePage.getContent(RenderContext.EditMode.NONE));

		} catch (Throwable t) {

			t.printStackTrace();
		}

		return buf.toString();
	}

}