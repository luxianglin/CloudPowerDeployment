# CloudPowerDeployment
基于强化学习的云计算虚拟机放置
云数据中心的高速发展带来了非常强大的计算能力，但是伴随产生的能耗问题也日益严重。为了降低云数据中心内物理服务器的能耗开销，首先利用强化学习对虚拟机放置问题进行建模，随后结合实际问题从状态聚合和时间信度两个方面对Q-Learning(λ)算法进行优化，最后通过云仿真平台CloudSim和实际数据集对虚拟机放置问题进行实验。仿真实验结果表明，与Q-Learning算法、Greedy算法和PSO算法相比，优化后的Q-Learning(λ)算法更有效地降低了物理服务器的能耗开销，同时针对不同数量的虚拟机放置请求也能够保证更好的结果，具有较强的实用价值。

相应论文：《基于强化学习下一种能耗优化的虚拟机放置策略》

注意：实验中的图表是用matlab画出的，因此如果希望实验自动生成相应折线图则需要安装matlab。

<img src="https://github.com/luxianglin/CloudPowerDeployment/blob/master/1.png" width="600" height="450" />

<img src="https://github.com/luxianglin/CloudPowerDeployment/blob/master/2.png" width="600" height="450" />

<img src="https://github.com/luxianglin/CloudPowerDeployment/blob/master/3.png" width="600" height="450" />

<img src="https://github.com/luxianglin/CloudPowerDeployment/blob/master/4.png" width="600" height="450" />
