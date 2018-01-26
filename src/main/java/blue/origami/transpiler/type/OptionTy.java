package blue.origami.transpiler.type;

import blue.origami.transpiler.CodeMap;
import blue.origami.transpiler.Env;

public class OptionTy extends SimpleTy {

	public OptionTy() {
		super("Option", 1);
	}

	@Override
	public Ty newGeneric(Ty m) {
		return new GenericTy(this, m);
	}

	@Override
	public int costMapThisTo(Env env, Ty fromTy, Ty toTy) {
		if (toTy.isGeneric(Ty.tOption)) {
			if (fromTy.getParamType() == Ty.tAnyRef || toTy.getParamType() == Ty.tAnyRef) {
				return CodeMap.BESTCAST;
			}
		}
		return CodeMap.STUPID;
	}

	@Override
	public CodeMap findMapThisTo(Env env, Ty fromTy, Ty toTy) {
		Ty paramTy = fromTy.getParamType();
		if (toTy.isGeneric(Ty.tOption)) {
			if (paramTy == Ty.tAnyRef || toTy.getParamType() == Ty.tAnyRef) {
				return new CodeMap(CodeMap.BESTCAST, "anycast", "%s", this, toTy);
			}
		} else if (paramTy.equals(toTy)) {
			return new CodeMap(CodeMap.BADCONV, "unwrap", "%s", toTy, fromTy);
		}
		return null;
	}

}
