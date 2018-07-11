package blue.origami.transpiler;

import blue.origami.transpiler.type.Ty;
import blue.origami.transpiler.type.GenericTy;
import blue.origami.transpiler.type.VarTy;

public interface NameHint {

	public Ty getType();

	// public boolean equalsName(String key);

	// a, a', a'', a2, a12, a$ => a
	public static String keyName(String name) {
		int loc = name.length() - 1;
		for (; loc > 0; loc--) {
			char c = name.charAt(loc);
			if (c != '\'' /* && c != '$' */ && !Character.isDigit(c) && c != '_') {
				break;
			}
		}
		return name.substring(0, loc + 1);
	}

	public static String flatName(String name) {
		return safeName(name.replace("_", "").toLowerCase());
	}

	static boolean isFlatName(String name) {
		return flatName(name).equals(name);
	}

	public static NameHint addNameHint(Env env, AST ns, Ty ty) {
		return addNameHint(env, ns, keyName(ns.getString()), ty);
	}

	public static NameHint addNameHint(Env env, AST ns, String name, Ty ty) {
		NameHint hint = env.get(name, NameHint.class);
		if (hint == null) {
			hint = new NameDecl(name, ty);
			env.getTranspiler().add(name, hint);
		} else {
			Ty hty = hint.getType();
			Ty hparam = hty.devar().getParamType();
			Ty tparam = ty.getParamType();
			if ((hty.devar() instanceof VarTy && !(ty.devar() instanceof VarTy)) || (hparam != null && hparam.devar() instanceof VarTy && tparam != null && !(tparam.devar() instanceof VarTy) && hty.devar() instanceof GenericTy && ty.devar() instanceof GenericTy && ((GenericTy) hty.devar()).getBaseType().eq(((GenericTy) ty.devar()).getBaseType()))) {
				hint = new NameDecl(name, ty);
				env.getTranspiler().add(name, hint);
			} else if (!hty.eq(ty)) {
				env.reportWarning(ns, TFmt.already_defined_YY1_as_YY2, name, hty);
			}
		}
		if (!isFlatName(name)) {
			String flatName = NameHint.flatName(name);
			// System.out.println("defining flat: " + flatName);
			NameHint hint2 = env.get(flatName, NameHint.class);
			if (hint2 == null) {
				env.getTranspiler().add(flatName, new NameDecl(name, ty));
			}
		}
		return hint;
	}

	public static Ty findNameHint(Env env, String name) {
		if (isOptional(name)) {
			Ty ty = findNameHint(env, name.substring(0, name.length() - 1));
			if (ty != null) {
				return Ty.tOption(ty);
			}
			return Ty.tBool;
		}
		if (isMutable(name)) {
			Ty ty = findNameHint(env, name.substring(0, name.length() - 1));
			if (ty != null) {
				return ty.toMutable();
			}
			return null;
		}
		NameHint hint = matchSubNames(env, keyName(name));
		if (hint == null && !isFlatName(name)) {
			hint = matchSubNames(env, flatName(name));
		}
		if (hint == null) {
			if (name.endsWith("s") || name.endsWith("*")) {
				Ty ty = findNameHint(env, name.substring(0, name.length() - 1));
				if (ty != null) {
					return Ty.tList(ty);
				}
			}
			return null;
		}
		return hint.getType();
	}

	static NameHint matchSubNames(Env env, String name) {
		NameHint hint = env.get(name, NameHint.class);
		if (hint == null && name.length() > 2) {
			return matchSubNames(env, name.substring(1));
		}
		return hint;
	}

	static boolean removeNameHint(Env env, AST ns) {
		return removeNameHint(env, keyName(ns.getString()));
	}

	static boolean removeNameHint(Env env, String name) {
		return env.getTranspiler().remove(name);
	}

	public static String safeName(String name) {
		return keyName(name.replace('?', 'Q'));
	}

	public static boolean isOneLetterName(String name) {
		int cnt = 0;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLowerCase(c) || Character.isUpperCase(c)) {
				cnt++;
			}
		}
		return cnt == 1;
	}

	public static boolean isOptional(String name) {
		return name.endsWith("?");
	}

	public static boolean isMutable(String name) {
		return name.endsWith("@") || name.endsWith("$");
	}

}

class NameDecl implements NameHint {
	Ty ty;

	NameDecl(String name, Ty ty) {
		this.ty = ty;
	}

	@Override
	public Ty getType() {
		return this.ty;
	}

}
