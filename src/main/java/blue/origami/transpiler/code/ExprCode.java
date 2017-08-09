package blue.origami.transpiler.code;

import java.util.ArrayList;
import java.util.List;

import blue.origami.transpiler.TCodeSection;
import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.TFmt;
import blue.origami.transpiler.Template;
import blue.origami.transpiler.Ty;
import blue.origami.transpiler.VarDomain;
import blue.origami.transpiler.VarTy;
import blue.origami.transpiler.code.CastCode.TBoxCode;
import blue.origami.transpiler.code.CastCode.TConvTemplate;
import blue.origami.transpiler.code.CastCode.TUnboxCode;
import blue.origami.util.ODebug;
import blue.origami.util.StringCombinator;

public class ExprCode extends CodeN {

	private String name;

	public ExprCode(Template tp, Code... args) {
		super(tp.getReturnType(), args);
		this.setTemplate(tp);
		this.name = tp.getName();
	}

	public ExprCode(String name, Code... args) {
		super(Ty.tUntyped, args);
		this.name = name;
	}

	@Override
	public void emitCode(TEnv env, TCodeSection sec) {
		sec.pushCall(env, this);
	}

	@Override
	public Code asType(TEnv env, Ty ret) {
		if (this.isUntyped()) {
			final Code[] params = this.args;
			List<Template> l = new ArrayList<>(8);
			env.findList(this.name, Template.class, l, (tt) -> !tt.isExpired() && tt.getParamSize() == params.length);
			if (l.size() == 0) {
				env.findList(this.name, Template.class, l, (tt) -> !tt.isExpired());
				throw new ErrorCode("undefined %s%s%s", this.name, this.types(params), this.hint(l));
			}
			if (l.size() == 1) {
				return this.asType(env, l.get(0).update(env, params), ret);
			}
			for (int i = 0; i < params.length; i++) {
				Ty pt = this.getCommonParamType(l, i);
				this.args[i] = this.args[i].asType(env, pt);
			}
			Template selected = l.get(0);
			int mapCost = this.checkMapCost(env, ret, selected);
			ODebug.trace("cost=%d,%s", mapCost, selected);
			for (int i = 1; i < l.size(); i++) {
				if (mapCost > 0) {
					Template next = l.get(i);
					int nextCost = this.checkMapCost(env, ret, next);
					ODebug.trace("nextcost=%d,%s", nextCost, next);
					if (nextCost < mapCost) {
						mapCost = nextCost;
						selected = next;
					}
				}
			}
			if (mapCost >= CastCode.STUPID) {
				throw new ErrorCode("mismatched %s%s%s", this.name, this.types(params), this.hint(l));
			}
			return this.asType(env, selected.update(env, params), ret);
		}
		return super.asType(env, ret);
	}

	public Code asType2(TEnv env, Ty t) {
		if (this.isUntyped()) {
			final Code[] params = this.args;
			List<Template> l = new ArrayList<>(8);
			env.findList(this.name, Template.class, l, (tt) -> !tt.isExpired() && tt.getParamSize() == params.length);
			// ODebug.trace("l = %s", l);
			if (l.size() == 0) {
				env.findList(this.name, Template.class, l, (tt) -> !tt.isExpired());
				throw new ErrorCode("undefined %s%s%s", this.name, this.types(params), this.hint(l));
			}
			if (l.size() == 1) {
				return this.asType(env, l.get(0).update(env, params), t);
			}
			boolean foundUntyped = false;
			for (int i = 0; i < params.length; i++) {
				Ty pt = this.getCommonParamType(l, i);
				this.args[i] = this.args[i].asType(env, pt);
				if (this.args[i].isUntyped()) {
					foundUntyped = true;
				}
			}
			if (foundUntyped == true) {
				return this;
			}
			Template selected = l.get(0);
			int mapCost = this.checkMapCost(env, t, selected);
			// ODebug.trace("cost=%d,%s", mapCost, selected);
			for (int i = 1; i < l.size(); i++) {
				if (mapCost > 0) {
					Template next = l.get(i);
					int nextCost = this.checkMapCost(env, t, next);
					// ODebug.trace("nextcost=%d,%s", nextCost, next);
					if (nextCost < mapCost) {
						mapCost = nextCost;
						selected = next;
					}
				}
			}
			if (mapCost >= CastCode.STUPID) {
				throw new ErrorCode("mismatched %s%s%s", this.name, this.types(params), this.hint(l));
			}
			return this.asType(env, selected.update(env, params), t);
		}
		return super.asType(env, t);
	}

	private String hint(List<Template> l) {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (Template tp : l) {
			if (c > 0) {
				sb.append(", ");
			}
			sb.append(tp.getName());
			sb.append(":: ");
			sb.append(tp.getFuncType());
			c++;
		}
		if (sb.length() == 0) {
			return "";
		}
		return " \t" + TFmt.hint + " " + sb.toString();
	}

	private String types(Code... params) {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		sb.append("(");
		for (Code t : params) {
			if (c > 0) {
				sb.append(", ");
			}
			sb.append(t.getType());
			c++;
		}
		sb.append(")");
		return sb.toString();
	}

	private Code asType(TEnv env, Template tp, Ty t) {
		if (tp != null) {
			Ty[] p = tp.getParamTypes();
			Ty ret = tp.getReturnType();
			if (tp.isGeneric()) {
				VarDomain dom = new VarDomain();
				Ty[] gp = new Ty[p.length];
				for (int i = 0; i < p.length; i++) {
					gp[i] = p[i].dupTy(dom);
				}
				ret = ret.dupTy(dom);
				for (int i = 0; i < this.args.length; i++) {
					this.args[i] = this.args[i].asType(env, gp[i]);
					if (p[i] instanceof VarTy) {
						ODebug.trace("must upcast %s => %s", p[i], gp[i]);
						this.args[i] = new TBoxCode(gp[i], this.args[i]);
					}
				}
				this.setTemplate(tp);
				this.setType(ret);
				Code result = this;
				if (tp.getReturnType() instanceof VarTy) {
					ODebug.trace("must downcast %s => %s", tp.getReturnType(), ret);
					result = new TUnboxCode(ret, result);
				}
				return result.castType(env, t);
			} else {
				for (int i = 0; i < this.args.length; i++) {
					this.args[i] = this.args[i].asType(env, p[i]);
				}
				this.setTemplate(tp);
				this.setType(ret);
				return this.castType(env, t);
			}
		}
		return this;
	}

	private Ty getCommonParamType(List<Template> l, int n) {
		Ty t = l.get(0).getParamTypes()[n];
		for (int i = 1; i < l.size(); i++) {
			if (!t.equals(l.get(i).getParamTypes()[n])) {
				return Ty.tUntyped;
			}
		}
		return t;
	}

	private int checkMapCost(TEnv env, Ty ret, Template tp) {
		int mapCost = 0;
		VarDomain dom = null;
		Ty[] p = tp.getParamTypes();
		Ty codeRet = tp.getReturnType();
		if (tp.isGeneric()) {
			dom = new VarDomain();
			Ty[] gp = new Ty[p.length];
			for (int i = 0; i < p.length; i++) {
				gp[i] = p[i].dupTy(dom);
			}
			p = gp;
			codeRet = codeRet.dupTy(dom);
		}
		for (int i = 0; i < this.args.length; i++) {
			Ty codeParamType = this.args[i].getType();
			if (p[i].acceptTy(true, codeParamType, false)) {
				continue;
			}
			TConvTemplate conv = env.findTypeMap(env, codeParamType, p[i]);
			ODebug.trace("mapcost[%d] %s => %s cost=%d", i, codeParamType, p[i], conv.mapCost);
			mapCost += conv.mapCost;
			if (mapCost >= CastCode.STUPID) {
				return mapCost;
			}
		}
		if (ret.isSpecific()) {
			if (!ret.acceptTy(true, codeRet, false)) {
				TConvTemplate conv = env.findTypeMap(env, codeRet, ret);
				ODebug.trace("mapcost[ret] %s => %s cost=%d", codeRet, ret, conv.mapCost);
				mapCost += (conv.mapCost * 2);
			}
		}
		if (dom != null) {
			mapCost += dom.mapCost();
		}
		return mapCost;
	}

	@Override
	public void strOut(StringBuilder sb) {
		sb.append("(");
		for (int i = 0; i < this.args.length; i++) {
			if (i > 1) {
				sb.append(" ");
			}
			StringCombinator.append(sb, this.args[i]);
		}
		sb.append(")");
	}

}