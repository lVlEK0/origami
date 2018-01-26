package blue.origami.transpiler.code;

import blue.origami.common.OArrays;
import blue.origami.common.ODebug;
import blue.origami.common.SyntaxBuilder;
import blue.origami.transpiler.CodeSection;
import blue.origami.transpiler.Env;
import blue.origami.transpiler.NameHint;
import blue.origami.transpiler.TFmt;
import blue.origami.transpiler.type.DataTy;
import blue.origami.transpiler.type.VarTy;
import blue.origami.transpiler.type.Ty;
import blue.origami.transpiler.code.DataCode;

public class DataEmptyCode extends DataCode {
  public DataEmptyCode() {
    super(true, OArrays.emptyNames, OArrays.emptyCodes);
  }

  @Override
	public Code asType(Env env, Ty ret) {
    this.setType(Ty.tData());
    return super.castType(env, ret);
	}
}
