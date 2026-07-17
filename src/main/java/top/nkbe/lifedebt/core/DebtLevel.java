package top.nkbe.lifedebt.core;

/**
 * 债务等级。债务不是单纯的数字，达到不同阈值会解锁不同的世界影响。
 *
 * <p>阈值表刻意集中在此处，方便后续做数值平衡时统一调整（未来可改为 datapack 驱动）。
 */
public enum DebtLevel {

	/** 正常：无效果。 */
	NORMAL(0),

	/** 借命者：出现粒子效果。 */
	BORROWER(5),

	/** 负债者：夜晚生成追债者。 */
	DEBTOR(10),

	/** 亡命者：世界出现异常（村民涨价等）。 */
	FUGITIVE(20),

	/** 死人未亡：触发终极世界事件。 */
	DEAD_NOT_GONE(50);

	/** 进入该等级所需的最低债务值。 */
	public final int threshold;

	DebtLevel(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * 根据当前债务值推导债务等级。等级为派生值，不单独持久化，避免与 debt 失去同步。
	 */
	public static DebtLevel fromDebt(int debt) {
		DebtLevel result = NORMAL;
		for (DebtLevel level : values()) {
			if (debt >= level.threshold) {
				result = level;
			}
		}
		return result;
	}
}
