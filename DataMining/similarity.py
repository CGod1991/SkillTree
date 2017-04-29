
def calculate_rating(user, thing1, user_ratings):
    """
    预测用户user对物品thing1的评价，user_ratings包含所有的评价数据
    使用加权求和的公式进行预测
    """

    ratings = user_ratings[user]
    num = 0.0
    denominator = 0.0
    for name in ratings:
        similarity_thing1 = similarity(thing1, name)    #获取相似度
        num += similarity_thing1 * ratings[name]
        denominator += abs(similarity_thing1)

    if denominator != 0.0:
        return num / denominator

    return None

def adjusted_rating(rating, max_r, min_r):
    """
    计算修正后的评分，即将评分换算至-1到1之间，主要是为了计算方便
    rating表示原始的评分数值，max_r表示评分系统的上限值，min_r表示评分系统的下限值
    """

    num = 2 *(rating - min_r) - (max_r - min_r)
    denominator = max_r - min_r
    if denominator != 0:
        return num / denominator

    return None
    
