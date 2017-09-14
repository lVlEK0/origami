package blue.origami.transpiler.code;

import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.type.Ty;
import blue.origami.util.OStrings;

public class DataRangeCode extends DataListCode {

	public DataRangeCode(Code start, Code end) {
		super(false, start, end);
	}

	@Override
	public Code asType(TEnv env, Ty ret) {
		if (this.isUntyped()) {
			this.args[0] = this.args[0].asType(env, Ty.tInt);
			this.args[1] = this.args[1].asType(env, Ty.tInt);
			this.setType(Ty.tList(Ty.tInt));
		}
		return super.asType(env, ret);
	}

	@Override
	public void strOut(StringBuilder sb) {
		sb.append(this.isMutable() ? "(" : "[");
		OStrings.append(sb, this.args[0]);
		sb.append(" to ");
		OStrings.append(sb, this.args[1]);
		sb.append(this.isMutable() ? ")" : "]");
	}

}