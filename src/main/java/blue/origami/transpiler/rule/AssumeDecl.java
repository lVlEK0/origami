package blue.origami.transpiler.rule;

import java.util.ArrayList;

import blue.origami.transpiler.AST;
import blue.origami.transpiler.Env;
import blue.origami.transpiler.NameHint;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.DoneCode;
import blue.origami.transpiler.code.ErrorCode;
import blue.origami.transpiler.type.Ty;
import blue.origami.transpiler.type.DataTy;
import blue.origami.transpiler.type.OptionTy;
import blue.origami.transpiler.type.GenericTy;
import blue.origami.transpiler.TFmt;
import blue.origami.common.ODebug;

public class AssumeDecl implements ParseRule, Symbols {

	private class AssumeName {
		String name;
		int index;
		AST position;
		AssumeName(String name, int index, AST position) {
			this.name = name;
			this.index = index;
			this.position = position;
		}
	}

	@Override
	public Code apply(Env env, AST t) {
		ArrayList<AST> bufAST = new ArrayList<AST>();
		ArrayList<AST> bufType = new ArrayList<AST>();
		ArrayList<AssumeName> bufNames = new ArrayList<AssumeName>();
		for (AST sub : t.get(_body)) {
			boolean flag = false;
			AST type = sub.get(_type);
			if (type.has(_base) && type.has(_param) && type.get(_base).getString().equals("Option")) {
				AST param = type.get(_param);
				if (param.is("DataType")) {
					for (AST p : param) {
						String name = p.getString();
						if (NameHint.findNameHint(env, name) == null) {
							bufNames.add(new AssumeName(name, bufAST.size(), p));
							flag = true;
						}
					}
				}
			}
			if (flag) {
				bufAST.add(sub);
				bufType.add(type);
			} else {
				Ty ty = env.parseType(env, type, null);
				for (AST ns : sub.get(_name)) {
					NameHint.addNameHint(env, ns, ty);
					// System.out.println("defined " + ns.getString() + " " +
					// env.findNameHint(ns.getString()));
				}
			}
		}
		for (AssumeName bufName : bufNames) {
			if (NameHint.findNameHint(env, bufName.name) == null) {
				NameHint.addNameHint(env, bufName.position, NameHint.keyName(bufName.name), Ty.tVar(null));
			} else {
				bufNames.remove(bufName);
			}
		}
		for (int i = 0; i < bufAST.size(); i++) {
			Ty ty = env.parseType(env, bufType.get(i), null);
			for (AST ns : bufAST.get(i).get(_name)) {
				NameHint.addNameHint(env, ns, ty);
			}
		}
		boolean flag = false;
		AssumeName error = null;
		for (AssumeName bufName : bufNames) {
			if (NameHint.findNameHint(env, bufName.name).devar().isVar()) {
				NameHint.removeNameHint(env, NameHint.keyName(bufName.name));
				for (AST ns : bufAST.get(bufName.index).get(_name)) {
					NameHint.removeNameHint(env, ns);
				}
				error = bufName;
				flag = true;
			}
		}
		if (flag) {
			return new ErrorCode(error.position, TFmt.no_type_hint__YY1, error.name);
		}
		return new DoneCode();
	}

}
