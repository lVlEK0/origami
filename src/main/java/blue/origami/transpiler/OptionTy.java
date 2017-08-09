package blue.origami.transpiler;

import blue.origami.util.StringCombinator;

public class OptionTy extends Ty {
	private Ty innerTy;

	public OptionTy(Ty ty) {
		this.innerTy = ty;
		assert !(ty instanceof OptionTy);
	}

	@Override
	public boolean isOption() {
		return true;
	}

	@Override
	public boolean hasVar() {
		return this.innerTy.hasVar();
	}

	@Override
	public Ty dupTy(VarDomain dom) {
		Ty inner = this.innerTy.dupTy(dom);
		if (inner != this.innerTy) {
			return new OptionTy(inner);
		}
		return this;
	}

	@Override
	public boolean acceptTy(boolean sub, Ty t, boolean updated) {
		if (t instanceof OptionTy) {
			return this.innerTy.acceptTy(sub, ((OptionTy) t).innerTy, updated);
		}
		if (t instanceof VarTy) {
			return (t.acceptTy(false, this, updated));
		}
		return this.innerTy.acceptTy(sub, t, updated);
	}

	@Override
	public boolean isDynamic() {
		return this.innerTy.isDynamic();
	}

	@Override
	public Ty nomTy() {
		if (this.innerTy instanceof OptionTy) {
			return this.innerTy.nomTy();
		}
		Ty ty = this.innerTy.nomTy();
		if (this.innerTy != ty) {
			return Ty.tOption(ty);
		}
		return this;
	}

	@Override
	public String strOut(TEnv env) {
		return this.innerTy.strOut(env);
	}

	@Override
	public boolean isUntyped() {
		return this.innerTy.isUntyped();
	}

	@Override
	public void strOut(StringBuilder sb) {
		sb.append("Option[");
		StringCombinator.append(sb, this.innerTy);
		sb.append("]");
	}

	@Override
	public String key() {
		return this.innerTy + "?";
	}

}