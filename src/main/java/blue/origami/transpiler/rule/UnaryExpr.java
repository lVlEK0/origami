package blue.origami.transpiler.rule;

import blue.origami.transpiler.AST;
import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.code.Code;

public class UnaryExpr implements ParseRule, Symbols {

	final String op;

	public UnaryExpr(String op) {
		this.op = op;
	}

	@Override
	public Code apply(TEnv env, AST t) {
		Code expr = env.parseCode(env, t.get(_expr));
		return expr.applyMethodCode(env, this.op);
	}

}