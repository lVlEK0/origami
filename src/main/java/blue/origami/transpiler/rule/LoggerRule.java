package blue.origami.transpiler.rule;

import blue.origami.nez.ast.LocaleFormat;
import blue.origami.transpiler.AST;
import blue.origami.transpiler.TLog;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.LogCode;

public class LoggerRule {
	private TLog addMessage(TLog head, AST s, int level, LocaleFormat format, Object[] args) {
		return new TLog(head, s, level, format, args);
	}

	public TLog reportError(TLog log, AST s, LocaleFormat fmt, Object... args) {
		return this.addMessage(log, s, TLog.Error, fmt, args);
	}

	public TLog reportWarning(TLog log, AST s, LocaleFormat fmt, Object... args) {
		return this.addMessage(log, s, TLog.Warning, fmt, args);
	}

	public TLog reportNotice(TLog log, AST s, LocaleFormat fmt, Object... args) {
		return this.addMessage(log, s, TLog.Notice, fmt, args);
	}

	public Code log(TLog log, Code code) {
		if (log == null) {
			return code;
		}
		return new LogCode(log, code);
	}

}
