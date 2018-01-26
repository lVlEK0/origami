package blue.origami.transpiler.type;

import blue.origami.common.ODebug;

public class FlowDataTy extends DataTy {

	public FlowDataTy() {
		super();
	}

	private boolean hasMutation = true;

	@Override
	public boolean hasMutation() {
		return this.hasMutation;
	}

	@Override
	public void foundMutation() {
		this.hasMutation = true;
	}

	@Override
	public Ty memoed() {
		//ODebug.TODO();
		return Ty.tData(this.hasMutation, this.getId(), this.names());
	}

	@Override
	public boolean hasField(String field, TypeMatcher logs) {
		if (!super.hasField(field, logs)) {
			if (logs.isUpdate()) {
				this.fields.add(field);
			}
		}
		return true;
	}

}
