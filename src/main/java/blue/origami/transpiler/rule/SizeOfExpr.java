package blue.origami.transpiler.rule;

import blue.origami.transpiler.AST;
import blue.origami.transpiler.Env;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.DataCode;
import blue.origami.transpiler.code.IntCode;
import blue.origami.transpiler.type.Ty;
import blue.origami.transpiler.type.FlowDataTy;
import blue.origami.transpiler.type.DataTy;
import blue.origami.transpiler.type.TypeMatcher;
import blue.origami.common.ODebug;

public class SizeOfExpr implements ParseRule, Symbols {
	@Override
	public Code apply(Env env, AST t) {
		Code recv = env.parseCode(env, t.get(_expr));
		if (recv instanceof DataCode) {
			return new IntCode(((DataCode)recv).getNames().length);
		} else {
			Ty recvTy = recv.asType(env, Ty.tUntyped()).getType();
			if (recvTy == null) {
				return recv.applyMethodCode(env, "||");
			}
			if (recvTy.isVar()) {
				Ty infer = new FlowDataTy();
				recvTy.match(true, infer, TypeMatcher.Update);
				recvTy = infer;
			}
			if (recvTy.isData()) {
				return new IntCode(((DataTy) recvTy.base()).size());
			}
		}
		return recv.applyMethodCode(env, "||");
	}
}
