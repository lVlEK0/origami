package blue.origami.chibi;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*import blue.origami.chibi.Func.FuncIntObj;
import blue.origami.chibi.Func.FuncObjBool;
import blue.origami.chibi.Func.FuncObjFloat;
import blue.origami.chibi.Func.FuncObjInt;
import blue.origami.chibi.Func.FuncObjObj;
import blue.origami.chibi.Func.FuncObjObjObj;
import blue.origami.chibi.Func.FuncObjVoid;*/
import blue.origami.common.OStrings;
import blue.origami.chibi.List$;

public class List$Char implements OStrings/*, FuncIntObj */{
	protected char[] arrays = null;
	protected int start = 0;
	protected int end = 0;
	protected List$Char next;

	List$Char(char[] arrays, int start, int end, List$Char next) {
		this.arrays = arrays;
		this.start = start;
		this.end = end;
		this.next = next;
	}

	List$Char(char[] arrays, int start, int end) {
		this(arrays, start, end, null);
	}

	public List$Char(char[] arrays) {
		this(arrays, 0, arrays.length, null);
	}

	public static final List$Char newArray(char[] arrays) {
		return new List$Char(arrays);
	}

	public List$Char bind() {
		return this;
	}

	public int size() {
		int len = 0;
		for (List$Char p = this; p != null; p = p.next) {
			len += p.end - p.start;
		}
		return len;
	}

	public List$Char connect(List$Char last) {
		List$Char p = this;
		while (p.next != null) {
			p = p.next;
		}
		p.next = last;
		return this;
	}

	private void flatten() {
		if (this.next != null) {
			char[] buf = new char[this.size()];
			int offset = 0;
			for (List$Char p = this; p != null; p = p.next) {
				System.arraycopy(p.arrays, p.start, buf, offset, p.end - p.start);
				offset += p.end - p.start;
			}
			this.arrays = buf;
			this.start = 0;
			this.end = offset;
			this.next = null;
		}
	}

	private int oneSize() {
		return this.end - this.start;
	}

	public char geti(int index) {
		List$Char p = this;
		int pos = index;
		while (p != null && p.oneSize() <= pos) {
			pos -= p.oneSize();
			p = p.next;
		}

		if (p != null) {
			return p.arrays[p.start + pos];
		} else {
			return '\0';
		}
	}

	private List$Char reSlice(List$Char p, int remains) {
		if (p == null) {
			return new List$Char(new char[0]);
		} else if (remains <= p.oneSize()) {
			return new List$Char(p.arrays, p.start, p.start + remains);
		} else {
			p.next = reSlice(p.next, remains - p.oneSize());
			return p;
		}
	}

	public List$Char slice(int left, int right) {
		List$Char p = this;
		int pos = 0;
		int offset = 0;
		while (p != null && offset + p.oneSize() <= left) {
			offset += p.oneSize();
			p = p.next;
		}

		if (p == null) {
			return new List$Char(new char[0]);
		} else if (right <= offset + p.oneSize()) {
			return new List$Char(p.arrays, p.start + left - offset, p.start + right - offset);
		} else {
			return new List$Char(p.arrays, p.start + left - offset, p.end, reSlice(p.next, right - offset - p.oneSize()));
		}
	}

	public void seti(int index, char value) {
		this.flatten();
		this.arrays[this.start + index] = value;
		return;
	}

	public static List$Char cons(char x, List$Char xs) {
		char[] a = { x };
		return new List$Char(a, 0, 1, xs);
	}

	public List$Char tail(int shift) {
		this.flatten();
		return new List$Char(this.arrays, this.start + shift, this.end);
	}

	public List$Char head(int shift) {
		this.flatten();
		return new List$Char(this.arrays, this.start, this.end - shift);
	}

	/*@Override
	public char applyC(int v) {
		return this.geti(v);
	}*/

	public final static void p(List$Char a) {
		System.out.println(a.toString());
	}

	@Override
	public String toString() {
		return OStrings.stringfy(this);
	}

	@Override
	public void strOut(StringBuilder sb) {
		int cnt = 0;
		sb.append("[");
		for (List$Char p = this; p != null; p = p.next) {
			cnt = this.strOut(sb, p, cnt);
		}
		sb.append("]");
	}

	private int strOut(StringBuilder sb, List$Char p, int cnt) {
		for (int i = p.start; i < p.end; i++) {
			if (cnt > 0) {
				sb.append(",");
			}
			sb.append("'");
			sb.append(p.arrays[i]);
			sb.append("'");
			cnt++;
		}
		return cnt;
	}

	private void ensure(int capacity) {
		if (this.arrays == null) {
			this.arrays = new char[Math.max(4, capacity)];
		} else if (this.arrays.length <= capacity) {
			char[] na = new char[Math.max(this.arrays.length * 2, capacity)];
			System.arraycopy(this.arrays, 0, na, 0, this.arrays.length);
			this.arrays = na;
		}
	}

	public void push(char v) {
		this.ensure(this.end);
		this.arrays[this.end++] = v;
	}

	public char pop() {
		this.end--;
		return this.arrays[this.end];
	}

	public String castToString() {
		this.flatten();
		char[] ca = new char[this.end - this.start];
		System.arraycopy(this.arrays, this.start, ca, 0, this.end - this.start);
		return String.valueOf(ca);
	}

	public static List$Char castFromString(String s) {
		char[] cs = s.toCharArray();
		return new List$Char(cs);
	}

	/* High-order functions */
/*
	public Stream stream() {
		Stream s = Arrays.stream(this.arrays, this.start, this.end);
		if (this.next != null) {
			return IntStream.concat(s, this.next.stream());
		}
		return s;
	}

	public static final List$Char list(IntStream s) {
		return new List$Char(s.toArray());
	}

	public final void forEach(FuncIntVoid f) {
		forEach(this.stream(), f);
	}

	public static final void forEach(IntStream s, FuncIntVoid f) {
		s.forEach(f);
	}

	public final void filter(FuncIntBool f) {
		filter(this.stream(), f);
	}

	public static final IntStream filter(IntStream s, FuncIntBool f) {
		return s.filter(f);
	}

	public final IntStream map(FuncIntInt f) {
		return map(this.stream(), f);
	}

	public static final IntStream map(IntStream s, FuncIntInt f) {
		return s.map(f);
	}

	public final Stream<Object> map(FuncIntObj f) {
		return map(this.stream(), f);
	}

	public static final Stream<Object> map(IntStream s, FuncIntObj f) {
		return s.mapToObj(f);
	}

	public final DoubleStream map(FuncIntFloat f) {
		return map(this.stream(), f);
	}

	public static final DoubleStream map(IntStream s, FuncIntFloat f) {
		return s.mapToDouble(f);
	}

	public static final IntStream downCast(Object o) {
		if (o instanceof IntStream) {
			return (IntStream) o;
		}
		return ((List$Char) o).stream();
	}

	public final IntStream flatMap(FuncIntObj f) {
		return flatMap(this.stream(), f);
	}

	public static final IntStream flatMap(IntStream s, FuncIntObj f) {
		return s.flatMap(x -> downCast(f.apply(x)));
	}

	public final int reduce(int acc, FuncIntIntInt f) {
		return reduce(this.stream(), acc, f);
	}

	public static final int reduce(IntStream s, int acc, FuncIntIntInt f) {
		return s.reduce(acc, f);
	}*/
}
