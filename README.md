This project explores possibilities of auto-adjust behaviour related parameters with labeled data. 

# 一级标题
## 二级标题
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题

# How this works
The who project is currently based on MATSim, version 14.0, 
# Calibrating procedure
# Evaluation
Because the goal is not using full population, it makes no sense comparing simulated link volumes. The following aspects are suggested:
1. Basic statistics
    - modal split total mode shares across ? 
    - prior mode shares & travel times with different activities: unlabeled user group with calibration VS without/initial calibration (mode share)  

        | Mode         | home->work    | work->home    | edu->home     | other->home  | work->other   | xxx->xxx      |  
        |--------------|---------------|---------------|---------------|--------------|---------------|---------------|  
        | **Car**      | -             | -             | -             | -            | -             | -             |  
        | **PT**       | -             | -             | -             | -            | -             | -             |  
        | **Walk**     | -             | -             | -             | -            | -             | -             |  
        | **std travel time** | -             | -             | -             | -            | -             | -             |  
        | **avg travel time** | -             | -             | -             | -            | -             | -             |  

2. Confusion Matrix Evaluation 
   Labeled user group ground truth selectedIsMax defaultParam VS MNL VS CMA-ES
   unlabeled users from full sample scenario vs 
   所有分类评估指标都基于混淆矩阵（Confusion Matrix） ：
    TP（True Positive） ：预测正确且为正类。 预测正确的样本数 
    TN（真负例） ：预测正确且为负类。  其他类别正确预测的样本数（通常用于二分类）
    FP（假正例/误报） ：将负类错误预测为正类。  实际不属于该类别但被误预测为该类别的样本数
    FN（假负例/漏报） ：将正类错误预测为负类。  实际属于该类别但被误预测为其他类别的样本数  
    **混淆矩阵 (Confusion Matrix)：**

    | 真实值 ↓ / 预测值 →             | Positive (selectedPlanScoreIsMax) | Negative (otherPlanScoreIsMax) |  
    |:------------------------------|----------------------------------:|-------------------------------:|  
    |Positive (labeled data是100%的) |                          TP (真正例) |                       FN (假负例) |  
    |Negative                      |                        FP (假正例) 0 |                       TN (真负例) |  

    1. 准确率 (Accuracy)
        计算模型整体预测正确的比例：  
        Accuracy= (TP + TN)/(TP + TN + FP + FN)  
        适用场景 ：数据分布平衡时（正负类样本数量接近）。
        局限性 ：对不平衡数据不敏感。例如，如果95%的样本是负类，模型全预测为负类也会得到95%的高准确率，但毫无意义。
    2. 精确率 (Precision)
        表示模型在预测该类别时的准确性，高精确率意味着预测为该类别的样本大部分是正确的。衡量预测为正类中实际为正类的比例（避免“假阳性”）：   
          Precision(P) = TP/(TP + FP)  
        适用场景 ：关注误报的代价很高时。例如，垃圾邮件分类中，模型错误标记正常邮件为垃圾邮件（FP），会严重打扰用户。
    3. 召回率 (Recall, 或 Sensitivity)  
        衡量实际正类中有多少被正确预测出来（避免“假阴性”）：  
        Recall(R)= TP/(TP + FN)  
        适用场景 ：关注漏报的代价很高时。例如，疾病诊断中，模型将患病样本错误分类为健康（FN），可能导致患者错过治疗。
    4. F1 分数 (F1-Score)  
        精确率和召回率的调和平均值，综合两者性能：  
        F1=2×(P×R)/(P+R)  
        适用场景 ：当需要平衡精确率和召回率时，尤其是数据不平衡的情况下。
        直接展示多分类的预测结果分布，便于直观分析。  
    Because FP=TN===0, so all above are equal...
3. Scaled link volumes count, default vs MNL vs CMA-ES
4. travel time distribution or KS test
    - hist diagram of travel time, departure time OR ks test
    - default vs MNL vs CMA-ES
5. advanced, use a full population scenario, with parameters set to default, then sample some as labeled, use them to calibrate parameters.   
    compare: 
   1. Cosine similarity of parameters estimated vs original
   2. pt line changes & link changes, affected people's mode share, travel time changes original vs cma-es vs MNL
    
