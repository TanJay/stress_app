from hrv import get_nn_intervals, remove_outliers, interpolate_nan_values, remove_ectopic_beats, get_time_domain_features, get_frequency_domain_features

def preprocess(rr):
    rr_intervals_without_outliers = remove_outliers(rr_intervals=rr,  low_rri=300, high_rri=2000)
    # This replace outliers nan values with linear interpolation
    interpolated_rr_intervals = interpolate_nan_values(rr_intervals=rr_intervals_without_outliers, interpolation_method="linear")

    # This remove ectopic beats from signal
    nn_intervals_list = remove_ectopic_beats(rr_intervals=interpolated_rr_intervals, method="malik")
    # This replace ectopic beats nan values with linear interpolation
    interpolated_nn_intervals = interpolate_nan_values(rr_intervals=nn_intervals_list)
    return interpolated_nn_intervals

def say_my_name(rr):
    pre = preprocess(rr)
    return [get_time_domain_features(pre), get_frequency_domain_features(pre)]