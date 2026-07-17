package top.nkbe.lifedebt.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 命债核心数据。所有玩法都依赖这一层，通过 {@link LifeDebtAttachments} 持久化挂载在玩家身上。
 *
 * <p>刻意做成可变对象：服务端逻辑取出后就地修改字段即可，附着系统会在存档时用
 * {@link #CODEC} 序列化当前引用的状态。债务等级 {@link DebtLevel} 为派生值，不单独存储。
 */
public class LifeDebtData {

	public static final Codec<LifeDebtData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("debt", 0).forGetter(d -> d.debt),
			Codec.INT.optionalFieldOf("borrowedLife", 0).forGetter(d -> d.borrowedLife),
			Codec.INT.optionalFieldOf("deathCount", 0).forGetter(d -> d.deathCount),
			ContractType.CODEC.optionalFieldOf("contract", ContractType.NONE).forGetter(d -> d.contract),
			Codec.LONG.optionalFieldOf("contractEnd", 0L).forGetter(d -> d.contractEnd),
			Codec.INT.optionalFieldOf("totemCharge", 0).forGetter(d -> d.totemCharge),
			Codec.BOOL.optionalFieldOf("starterContractGranted", false).forGetter(d -> d.starterContractGranted)
	).apply(instance, LifeDebtData::new));

	/** 当前债务。 */
	private int debt;

	/** 累积借命次数。 */
	private int borrowedLife;

	/** 死亡次数。 */
	private int deathCount;

	/** 当前契约。 */
	private ContractType contract;

	/** 契约结束时刻（世界时间刻）。0 表示无有效契约。 */
	private long contractEnd;

	/** 当前图腾剩余容量（剩余可借命次数）。 */
	private int totemCharge;

	/** Whether the one-time starter Totem contract has been granted. */
	private boolean starterContractGranted;

	/** 空数据，供附着系统在玩家首次访问时初始化。 */
	public LifeDebtData() {
		this(0, 0, 0, ContractType.NONE, 0L, 0, false);
	}

	public LifeDebtData(int debt, int borrowedLife, int deathCount, ContractType contract, long contractEnd, int totemCharge) {
		this(debt, borrowedLife, deathCount, contract, contractEnd, totemCharge, false);
	}

	public LifeDebtData(int debt, int borrowedLife, int deathCount, ContractType contract, long contractEnd, int totemCharge,
			boolean starterContractGranted) {
		this.debt = debt;
		this.borrowedLife = borrowedLife;
		this.deathCount = deathCount;
		this.contract = contract;
		this.contractEnd = contractEnd;
		this.totemCharge = totemCharge;
		this.starterContractGranted = starterContractGranted;
	}

	public int getDebt() {
		return debt;
	}

	public void setDebt(int debt) {
		this.debt = Math.max(0, debt);
	}

	/** 增加债务并返回增加后的值。 */
	public int addDebt(int amount) {
		setDebt(this.debt + amount);
		return this.debt;
	}

	public int getBorrowedLife() {
		return borrowedLife;
	}

	public void setBorrowedLife(int borrowedLife) {
		this.borrowedLife = Math.max(0, borrowedLife);
	}

	public int getDeathCount() {
		return deathCount;
	}

	public void setDeathCount(int deathCount) {
		this.deathCount = Math.max(0, deathCount);
	}

	public ContractType getContract() {
		return contract;
	}

	public void setContract(ContractType contract) {
		this.contract = contract == null ? ContractType.NONE : contract;
	}

	public long getContractEnd() {
		return contractEnd;
	}

	public void setContractEnd(long contractEnd) {
		this.contractEnd = contractEnd;
	}

	public int getTotemCharge() {
		return totemCharge;
	}

	public void setTotemCharge(int totemCharge) {
		this.totemCharge = Math.max(0, totemCharge);
	}

	public boolean isStarterContractGranted() {
		return starterContractGranted;
	}

	public void setStarterContractGranted(boolean starterContractGranted) {
		this.starterContractGranted = starterContractGranted;
	}

	/** 当前债务等级，由 {@link #debt} 派生。 */
	public DebtLevel getLevel() {
		return DebtLevel.fromDebt(debt);
	}
}
