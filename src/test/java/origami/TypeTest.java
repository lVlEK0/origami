/***********************************************************************
 * Copyright 2017 Kimio Kuramitsu and ORIGAMI project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************/

package origami;

import blue.origami.Version;
import blue.origami.common.OConsole;
import blue.origami.parser.Parser;
import blue.origami.parser.peg.Grammar;
import blue.origami.parser.peg.SourceGrammar;
import blue.origami.transpiler.TFmt;
import blue.origami.transpiler.Transpiler;
import blue.origami.transpiler.type.Ty;

public class TypeTest extends CommandTest {

	public void testLiteral() throws Throwable {
		runScript("()", "()");
		runScript("true", "Bool");
		runScript("false", "Bool");
		runScript("1", "Int");
		runScript("1.0", "Float");
		runScript("'a'", "Char");
		runScript("'abc'", "String");
		runScript("\"abc\"", "String");
		runScript("[1,2,3]", "List[Int]");
		runScript("$[1,2,3]", "$List[Int]");
	}

	public void testHelloWorld() throws Throwable {
		runScript("println('hello,world')", "()");
	}

	public void testBinary() throws Throwable {
		runScript("1+1.0", "Float");
		runScript("1.0+1", "Float");
	}

	public void testLet() throws Throwable {
		runScript("a = 1\na", "Int");
	}

	public void testParamType() throws Throwable {
		runScript("f(a:Int)=a;f", "Int->Int");
		runScript("f(a:Option[a])=a;f", "Option[a]->Option[a]");
		runScript("f(a:List[a])=a;f", "List[a]->List[a]");
		runScript("f(a:$List[a]):$List[a]=a;f", "$List[a]->$List[a]");
		runScript("f(a:$List[a])=a;f", "$List[a]->List[a]");
	}

	public void testLambda() throws Throwable {
		runScript("\\n n+1", "Int->Int");
		runScript("\\a : Int a+1", "Int->Int");
		runScript("\\() 1", "()->Int");
		runScript("\\a \\b a+b", "(a,b)->c");
	}

	public void testRec() throws Throwable {
		runScript("sum(a: Int) = if a == 0 then 0 else a + sum(a-1);sum", "Int->Int");
		FIXME("sum(a) = if a == 0 then 0 else a + sum(a-1);sum", "a->Int");
	}

	public void testTemplate() throws Throwable {
		runScript("f(a)=|a|;f", "a->Int");
		runScript("f(a)=|a|;f(1);f", "Int->Int");
		runScript("f(a,n)={m=a[n];m};f", "(List[a],Int)->a");
		runScript("f(a,n)={m=a[n];m};f([0,1], 0);f", "(List[Int],Int)->Int");
		runScript("f(a, b) =\n  | (1, 1) -> 1\n  | (1, 0) -> 1\nf", "(Int,Int)->Int");
	}

	public void testAdHoc() throws Throwable {
		runScript("f(a)=2a+1;f", "a->b");
		runScript("f(a)=2a+1;f(1);f", "Int->Int");
		runScript("f(a)=2a+1;f(1.0);f", "Float->Float");
	}

	public void testHighOrderFunc() throws Throwable {
		runScript("f(g,a,b)=g(a,b);f", "((a,b)->c,a,b)->c");
		// f(\a \b a+b, 1, 2)
	}

	public void testIdentity() throws Throwable {
		runScript("f(a)=a;f", "a->a");
		runScript("f(a)=a;f(f);f", "(a->a)->(a->a)");
	}

	public void testOption() throws Throwable {
		runScript("Some(1)", "Option[Int]");
		runScript("Some(1) >>= (\\n Some(n+1))", "Option[Int]");
	}

	public void testIntList() throws Throwable {
		runScript("a=[1,2];a", "List[Int]");
		runScript("1::[]", "List[Int]");
	}

	public void testTuple() throws Throwable {
		runScript("(1, \"a\")", "Int*String");
		runScript("a=(1, \"a\");a", "Int*String");
		runScript("(a,b)=(1, \"a\");a", "Int");
		runScript("f(a,b)=(a,b);f", "(a,b)->a*b");
	}

	public void testData() throws Throwable {
		runScript("f(p) = p.x; f", "{x}->Float");
		runScript("f(p) = p.x + p.y; f", "{x,y}->Float");
	}

	public void testMutation() throws Throwable {
		runScript("f()=$[1,2];f", "()->List[Int]");
		runScript("f(a)=a[0];f", "List[a]->a");
		runScript("f(a)=a[0]=1;f", "$List[Int]->()");
		FIXME("f(a)={a[0];a[0]=1};f", "$List[Int]->()");
	}

	//
	static Grammar g = null;
	static Parser p = null;

	static Grammar g() throws Throwable {
		if (g == null) {
			g = SourceGrammar.loadFile(Version.ResourcePath + "/grammar/chibi.opeg");
		}
		return g;
	}

	static Parser p() throws Throwable {
		if (p == null) {
			p = g().newParser();
		}
		return p;
	}

	public static void runScript(String text, String checked) throws Throwable {
		Transpiler env = new Transpiler(g(), p());
		Ty ty = env.testType(text);
		System.out.printf("%s %s :: %s\n", TFmt.Checked, text, ty);
		if (checked != null) {
			assert (checked.equals(ty.toString())) : ty + " != " + checked;
		}
	}

	public static void FIXME(String text, String checked) throws Throwable {
		Transpiler env = new Transpiler(g(), p());
		Ty ty = env.testType(text);
		if (checked.equals(ty.toString())) {
			System.out.printf("%s %s :: %s\n", TFmt.Checked, text, ty);
		} else {
			System.out.printf(OConsole.color(OConsole.Red, "FIXME %s %s :: %s != %s\n"), TFmt.Checked, text, ty,
					checked);
		}
	}

}
