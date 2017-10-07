package blue.origami.transpiler.rule;

import blue.origami.transpiler.AST;
import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.IfCode;
import blue.origami.transpiler.code.MultiCode;

public class IfExpr implements ParseRule, Symbols {

	@Override
	public Code apply(TEnv env, AST t) {
		Code condCode = env.parseCode(env, t.get(_cond));
		Code thenCode = env.parseCode(env, t.get(_then));
		Code elseCode = t.has(_else) ? env.parseCode(env, t.get(_else)) : new MultiCode();
		return new IfCode(condCode, thenCode, elseCode);
	}

}
