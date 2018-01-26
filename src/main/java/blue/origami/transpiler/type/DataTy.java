package blue.origami.transpiler.type;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import blue.origami.common.OArrays;
import blue.origami.common.OStrings;
import blue.origami.transpiler.AST;
import blue.origami.transpiler.Env;
import blue.origami.transpiler.NameHint;
import blue.origami.transpiler.TFmt;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.DataCode;
import blue.origami.transpiler.code.ErrorCode;

public class DataTy extends Ty {
	boolean isMutable = true;
	private String cnt = "";

	@Override
	public boolean isMutable() {
		return this.isMutable;
	}

	@Override
	public Ty toImmutable() {
		if (this.isMutable()) {
			return Ty.tRecord(this.getId(), this.names());
		}
		return this;
	}

	TreeSet<String> fields;

	public DataTy() {
		this.isMutable = true;
		this.fields = new TreeSet<>();
	}

	public DataTy(boolean isMutable) {
		this.isMutable = isMutable;
		this.fields = new TreeSet<>();
	}

	public DataTy(boolean isMutable, String... names) {
		this(isMutable);
		for (String n : names) {
			this.fields.add(n);
		}
		this.getCnt();
	}

	public DataTy(boolean isMutable, int id, String... names) {
		this(isMutable);
		this.cnt = id == -1 ? "" : makeCnt(id);

		if (names.length != 0 && this.hasCnt(names[0])) {
			for (String n : names) {
				this.fields.add(n);
			}
		}else{
			for (String n : names) {
				if (n.isEmpty()) {
					continue;
				}
				this.fields.add(n + this.cnt);
			}
		}
	}

	public String[] names() {
		if (this.fields == null || this.fields.size() == 0) {
			return OArrays.emptyNames;
		}
		return this.fields.toArray(new String[this.fields.size()]);
	}

	@Override
	public Code getDefaultValue() {
		return new DataCode(this.isMutable ? Ty.tData() : Ty.tRecord());
	}

	public int size() {
		if (this.fields == null) {
			return 0;
		}
		return this.fields.size();
	}

	public String getCnt() {
		if (this.cnt.length() > 2) {
			return this.cnt;
		} else if (this.size() > 0){
			String first = this.fields.first();
			int index = first.lastIndexOf('_');
			if (index > 0) {
				this.cnt = first.substring(index);
				return this.cnt;
			}
		}
		return "";
	}

	public int getId() {
		String c = this.getCnt();
		if (c.length() > 2) {
			return Integer.parseInt(c.substring(1, c.length() - 1));
		}
		return -1;
	}

	public final boolean hasField(String field) {
		return this.hasField(field, TypeMatcher.Update);
	}

	public boolean hasField(String field, TypeMatcher logs) {
		if (DataTy.hasCnt(field)) {
			return this.hasField(deleteCnt(field), logs);
		}
		return this.fields.contains(field + this.getCnt());
	}

	public Ty fieldTy(Env env, AST s, String name) {
		if (!(this.hasCnt(name)) && this.cnt.length() > 1) {
			return this.fieldTy(env, s, name + this.getCnt());
		}
		if (this.hasField(name)) {
			NameHint hint = env.findGlobalNameHint(env, name);
			if (hint != null) {
				Ty ty = hint.getType();
				return ty == Ty.tThis ? this : ty;
			} else {
				hint = env.findGlobalNameHint(env, this.deleteCnt(name));
				if (hint != null) {
					Ty ty = hint.getType();
					env.addGlobalName(env, name, ty);
					return ty == Ty.tThis ? this : ty;
				}
			}
			throw new ErrorCode(s, TFmt.undefined_name__YY1, name);
		}
		throw new ErrorCode(s, TFmt.undefined_name__YY1_in_YY2, name, this);
	}

	private static boolean hasCnt(String name) {
		return name.lastIndexOf('_') != -1;
		//return true;
	}

	public static String makeCnt(int id) {
		return "_" + String.valueOf(id) + "D";
		//return "";
	}

	public static String deleteCnt(String name) {
		int index = name.lastIndexOf('_');
		if (index != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	public static String[] deleteCnts(String[] names) {
		String[] deletedNames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			deletedNames[i] = deleteCnt(names[i]);
		}
		return deletedNames;
	}

	@Override
	public void strOut(StringBuilder sb) {
		if (DataTy.this.isMutable) {
			sb.append(Ty.Mut);
		}
		sb.append("{");
		OStrings.joins(sb, this.names(), ",");
		sb.append("}");
	}

	@Override
	public boolean hasSome(Predicate<Ty> f) {
		return f.test(this);
	}

	@Override
	public Ty map(Function<Ty, Ty> f) {
		return f.apply(this);
	}

	@Override
	public Ty memoed() {
		return this;
	}

	@Override
	public Ty dupVar(VarDomain dom) {
		return this;
	}

	public final boolean hasFields(Set<String> fields, TypeMatcher logs) {
		for (String f : fields) {
			if (!this.hasField(f, logs)) {
				return false;
			}
		}
		return true;
	}

	// f(b)
	@Override
	public boolean match(boolean sub, Ty codeTy, TypeMatcher logs) {
		if (codeTy.isVar()) {
			// VarTy varTy = (VarTy) codeTy.real();
			// if (varTy.isParameter()) {
			DataTy pt = new FlowDataTy();
			pt.hasFields(this.fields, logs);
			return (codeTy.match(false, pt, logs));
			// }
			// return (codeTy.acceptTy(false, this, logs));
		}
		if (codeTy.isData()) {
			DataTy dt = (DataTy) codeTy.base();
			if (dt.hasFields(this.fields, logs)) {
				if (!sub) {
					return this.hasFields(dt.fields, logs);
				}
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public <C> C mapType(TypeMapper<C> codeType) {
		return codeType.forDataType(this);
	}

	@Override
	public String keyFrom() {
		return "{}";
	}

}
