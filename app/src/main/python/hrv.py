import pandas as pd
from typing import List, Tuple
from collections import namedtuple
import numpy as np
from scipy import interpolate
from scipy import signal

# Static name for methods params
MALIK_RULE = "malik"
KARLSSON_RULE = "karlsson"
KAMATH_RULE = "kamath"
ACAR_RULE = "acar"
CUSTOM_RULE = "custom"

# Frequency Methods name
WELCH_METHOD = "welch"
LOMB_METHOD = "lomb"

# Named Tuple for different frequency bands
VlfBand = namedtuple("Vlf_band", ["low", "high"])
LfBand = namedtuple("Lf_band", ["low", "high"])
HfBand = namedtuple("Hf_band", ["low", "high"])

def remove_outliers(rr_intervals: List[float], verbose: bool = True, low_rri: int = 300,
                    high_rri: int = 2000) -> list:
    """
    Function that replace RR-interval outlier by nan.
    Parameters
    ---------
    rr_intervals : list
        raw signal extracted.
    low_rri : int
        lowest RrInterval to be considered plausible.
    high_rri : int
        highest RrInterval to be considered plausible.
    verbose : bool
        Print information about deleted outliers.
    Returns
    ---------
    rr_intervals_cleaned : list
        list of RR-intervals without outliers
    References
    ----------
    .. [1] O. Inbar, A. Oten, M. Scheinowitz, A. Rotstein, R. Dlin, R.Casaburi. Normal \
    cardiopulmonary responses during incremental exercise in 20-70-yr-old men.
    .. [2] W. C. Miller, J. P. Wallace, K. E. Eggert. Predicting max HR and the HR-VO2 relationship\
    for exercise prescription in obesity.
    .. [3] H. Tanaka, K. D. Monahan, D. R. Seals. Age-predictedmaximal heart rate revisited.
    .. [4] M. Gulati, L. J. Shaw, R. A. Thisted, H. R. Black, C. N. B.Merz, M. F. Arnsdorf. Heart \
    rate response to exercise stress testing in asymptomatic women.
    """

    # Conversion RrInterval to Heart rate ==> rri (ms) =  1000 / (bpm / 60)
    # rri 2000 => bpm 30 / rri 300 => bpm 200
    rr_intervals_cleaned = [rri if high_rri >= rri >= low_rri else np.nan for rri in rr_intervals]

    if verbose:
        outliers_list = []
        for rri in rr_intervals:
            if high_rri >= rri >= low_rri:
                pass
            else:
                outliers_list.append(rri)

        nan_count = sum(np.isnan(rr_intervals_cleaned))
        if nan_count == 0:
            print("{} outlier(s) have been deleted.".format(nan_count))
        else:
            print("{} outlier(s) have been deleted.".format(nan_count))
            print("The outlier(s) value(s) are : {}".format(outliers_list))
    return rr_intervals_cleaned


def remove_ectopic_beats(rr_intervals: List[float], method: str = "malik",
                         custom_removing_rule: float = 0.2, verbose: bool = True) -> list:
    """
    RR-intervals differing by more than the removing_rule from the one proceeding it are removed.
    Parameters
    ---------
    rr_intervals : list
        list of RR-intervals
    method : str
        method to use to clean outlier. malik, kamath, karlsson, acar or custom.
    custom_removing_rule : int
        Percentage criteria of difference with previous RR-interval at which we consider
        that it is abnormal. If method is set to Karlsson, it is the percentage of difference
        between the absolute mean of previous and next RR-interval at which  to consider the beat
        as abnormal.
    verbose : bool
        Print information about ectopic beats.
    Returns
    ---------
    nn_intervals : list
        list of NN Interval
    outlier_count : int
        Count of outlier detected in RR-interval list
    References
    ----------
    .. [5] Kamath M.V., Fallen E.L.: Correction of the Heart Rate Variability Signal for Ectopics \
    and Miss- ing Beats, In: Malik M., Camm A.J.
    .. [6] Geometric Methods for Heart Rate Variability Assessment - Malik M et al
    """
    if method not in [MALIK_RULE, KAMATH_RULE, KARLSSON_RULE, ACAR_RULE, CUSTOM_RULE]:
        raise ValueError("Not a valid method. Please choose between malik, kamath, karlsson, acar.\
         You can also choose your own removing critera with custom_rule parameter.")

    if method == KARLSSON_RULE:
        nn_intervals, outlier_count = _remove_outlier_karlsson(rr_intervals=rr_intervals,
                                                               removing_rule=custom_removing_rule)

    elif method == ACAR_RULE:
        nn_intervals, outlier_count = _remove_outlier_acar(rr_intervals=rr_intervals)

    else:
        # set first element in list
        outlier_count = 0
        previous_outlier = False
        nn_intervals = [rr_intervals[0]]
        for i, rr_interval in enumerate(rr_intervals[:-1]):

            if previous_outlier:
                nn_intervals.append(rr_intervals[i + 1])
                previous_outlier = False
                continue

            if is_outlier(rr_interval, rr_intervals[i + 1], method=method,
                          custom_rule=custom_removing_rule):
                nn_intervals.append(rr_intervals[i + 1])
            else:
                nn_intervals.append(np.nan)
                outlier_count += 1
                previous_outlier = True

    if verbose :
        print("{} ectopic beat(s) have been deleted with {} rule.".format(outlier_count, method))

    return nn_intervals


def is_outlier(rr_interval: int, next_rr_interval: float, method: str = "malik",
               custom_rule: float = 0.2) -> bool:
    """
    Test if the rr_interval is an outlier
    Parameters
    ----------
    rr_interval : int
        RrInterval
    next_rr_interval : int
        consecutive RrInterval
    method : str
        method to use to clean outlier. malik, kamath, karlsson, acar or custom
    custom_rule : int
        percentage criteria of difference with previous RR-interval at which we consider
        that it is abnormal
    Returns
    ----------
    outlier : bool
        True if RrInterval is valid, False if not
    """
    if method == MALIK_RULE:
        outlier = abs(rr_interval - next_rr_interval) <= 0.2 * rr_interval
    elif method == KAMATH_RULE:
        outlier = 0 <= (next_rr_interval - rr_interval) <= 0.325 * rr_interval or 0 <= \
                  (rr_interval - next_rr_interval) <= 0.245 * rr_interval
    else:
        outlier = abs(rr_interval - next_rr_interval) <= custom_rule * rr_interval
    return outlier


def _remove_outlier_karlsson(rr_intervals: List[float], removing_rule: float = 0.2) -> Tuple[list, int]:
    """
    RR-intervals differing by more than the 20 % of the mean of previous and next RR-interval
    are removed.
    Parameters
    ---------
    rr_intervals : list
        list of RR-intervals
    removing_rule : float
        Percentage of difference between the absolute mean of previous and next RR-interval at which \
    to consider the beat as abnormal.
    Returns
    ---------
    nn_intervals : list
        list of NN Interval
    References
    ----------
    .. [7]  Automatic filtering of outliers in RR-intervals before analysis of heart rate \
    variability in Holter recordings: a comparison with carefully edited data - Marcus Karlsson, \
    Rolf Hörnsten, Annika Rydberg and Urban Wiklund
    """
    # set first element in list
    nn_intervals = [rr_intervals[0]]
    outlier_count = 0

    for i in range(len(rr_intervals)):
        # Condition to go out of loop at limits of list
        if i == len(rr_intervals)-2:
            nn_intervals.append(rr_intervals[i + 1])
            break
        mean_prev_next_rri = (rr_intervals[i] + rr_intervals[i+2]) / 2
        if abs(mean_prev_next_rri - rr_intervals[i+1]) < removing_rule * mean_prev_next_rri:
            nn_intervals.append(rr_intervals[i+1])
        else:
            nn_intervals.append(np.nan)
            outlier_count += 1
    return nn_intervals, outlier_count


def _remove_outlier_acar(rr_intervals: List[float], custom_rule=0.2) -> Tuple[list, int]:
    """
    RR-intervals differing by more than the 20 % of the mean of last 9 RrIntervals
    are removed.
    Parameters
    ---------
    rr_intervals : list
        list of RR-intervals
    custom_rule : int
        percentage criteria of difference with mean of  9 previous RR-intervals at
        which we consider that RR-interval is abnormal. By default, set to 20 %
    Returns
    ---------
    nn_intervals : list
        list of NN Interval
    References
    ----------
    .. [8] Automatic ectopic beat elimination in short-term heart rate variability measurements \
    Acar B., Irina S., Hemingway H., Malik M.
    """
    nn_intervals = []
    outlier_count = 0
    for i, rr_interval in enumerate(rr_intervals):
        if i < 9:
            nn_intervals.append(rr_interval)
            continue
        acar_rule_elt = np.nanmean(nn_intervals[-9:])
        if abs(acar_rule_elt - rr_interval) < custom_rule * acar_rule_elt:
            nn_intervals.append(rr_interval)
        else:
            nn_intervals.append(np.nan)
            outlier_count += 1
    return nn_intervals, outlier_count


def interpolate_nan_values(rr_intervals: list, interpolation_method: str = "linear", limit=1) -> list:
    """
    Function that interpolate Nan values with linear interpolation
    Parameters
    ---------
    rr_intervals : list
        RrIntervals list.
    interpolation_method : str
        Method used to interpolate Nan values of series.
    limit: int
        TODO
    Returns
    ---------
    interpolated_rr_intervals : list
        new list with outliers replaced by interpolated values.
    """
    series_rr_intervals_cleaned = pd.Series(rr_intervals)
    # Interpolate nan values and convert pandas object to list of values
    interpolated_rr_intervals = series_rr_intervals_cleaned.interpolate(method=interpolation_method,
                                                                        limit=limit,
                                                                        limit_area="inside")
    return interpolated_rr_intervals.values.tolist()


def get_nn_intervals(rr_intervals: List[float], low_rri: int = 300, high_rri: int = 2000,
                     interpolation_method: str = "linear", ectopic_beats_removal_method: str = KAMATH_RULE,
                     verbose: bool = True) -> List[float]:
    """
    Function that computes NN Intervals from RR-intervals.
    Parameters
    ---------
    rr_intervals : list
        RrIntervals list.
    interpolation_method : str
        Method used to interpolate Nan values of series.
    ectopic_beats_removal_method : str
        method to use to clean outlier. malik, kamath, karlsson, acar or custom.
    low_rri : int
        lowest RrInterval to be considered plausible.
    high_rri : int
        highest RrInterval to be considered plausible.
    verbose : bool
        Print information about deleted outliers.
    Returns
    ---------
    interpolated_nn_intervals : list
        list of NN Interval interpolated
    """
    rr_intervals_cleaned = remove_outliers(rr_intervals, low_rri=low_rri, high_rri=high_rri,
                                           verbose=verbose)
    interpolated_rr_intervals = interpolate_nan_values(rr_intervals_cleaned, interpolation_method)
    nn_intervals = remove_ectopic_beats(interpolated_rr_intervals,
                                        method=ectopic_beats_removal_method)
    interpolated_nn_intervals = interpolate_nan_values(nn_intervals, interpolation_method)
    return interpolated_nn_intervals


def is_valid_sample(nn_intervals: List[float], outlier_count: int, removing_rule: float = 0.04) -> bool:
    """
    Test if the sample meet the condition to be used for analysis
    Parameters
    ----------
    nn_intervals : list
        list of Normal to Normal Interval
    outlier_count : int
        count of outliers or ectopic beats removed from the interval
    removing_rule : str
        rule to follow to determine whether the sample is valid or not
    Returns
    ----------
    bool
        True if sample is valid, False if not
    """
    result = True
    if outlier_count / len(nn_intervals) > removing_rule:
        print("Too much outlier for analyses ! You should descard the sample.")
        result = False
    if len(nn_intervals) < 240:
        print("Not enough Heart beat for Nyquist criteria ! ")
        result = False
    return result


def get_time_domain_features(nn_intervals: List[float]) -> dict:
    """
    Returns a dictionary containing time domain features for HRV analysis.
    Mostly used on long term recordings (24h) but some studies use some of those features on
    short term recordings, from 1 to 5 minutes window.
    Parameters
    ----------
    nn_intervals : list
        list of Normal to Normal Interval
    Returns
    -------
    time_domain_features : dict
        dictionary containing time domain features for HRV analyses. There are details
        about each features below.
    Notes
    -----
    Here are some details about feature engineering...
    - **mean_nni**: The mean of RR-intervals.
    - **sdnn** : The standard deviation of the time interval between successive normal heart beats \
    (i.e. the RR-intervals).
    - **sdsd**: The standard deviation of differences between adjacent RR-intervals
    - **rmssd**: The square root of the mean of the sum of the squares of differences between \
    adjacent NN-intervals. Reflects high frequency (fast or parasympathetic) influences on hrV \
    (*i.e.*, those influencing larger changes from one beat to the next).
    - **median_nni**: Median Absolute values of the successive differences between the RR-intervals.
    - **nni_50**: Number of interval differences of successive RR-intervals greater than 50 ms.
    - **pnni_50**: The proportion derived by dividing nni_50 (The number of interval differences \
    of successive RR-intervals greater than 50 ms) by the total number of RR-intervals.
    - **nni_20**: Number of interval differences of successive RR-intervals greater than 20 ms.
    - **pnni_20**: The proportion derived by dividing nni_20 (The number of interval differences \
    of successive RR-intervals greater than 20 ms) by the total number of RR-intervals.
    - **range_nni**: difference between the maximum and minimum nn_interval.
    - **cvsd**: Coefficient of variation of successive differences equal to the rmssd divided by \
    mean_nni.
    - **cvnni**: Coefficient of variation equal to the ratio of sdnn divided by mean_nni.
    - **mean_hr**: The mean Heart Rate.
    - **max_hr**: Max heart rate.
    - **min_hr**: Min heart rate.
    - **std_hr**: Standard deviation of heart rate.
    References
    ----------
    .. [1] Heart rate variability - Standards of measurement, physiological interpretation, and \
    clinical use, Task Force of The European Society of Cardiology and The North American Society \
    of Pacing and Electrophysiology, 1996
    """

    diff_nni = np.diff(nn_intervals)
    length_int = len(nn_intervals)

    # Basic statistics
    mean_nni = np.mean(nn_intervals)
    median_nni = np.median(nn_intervals)
    range_nni = max(nn_intervals) - min(nn_intervals)

    sdsd = np.std(diff_nni)
    rmssd = np.sqrt(np.mean(diff_nni ** 2))

    nni_50 = sum(np.abs(diff_nni) > 50)
    pnni_50 = 100 * nni_50 / length_int
    nni_20 = sum(np.abs(diff_nni) > 20)
    pnni_20 = 100 * nni_20 / length_int

    # Feature found on github and not in documentation
    cvsd = rmssd / mean_nni

    # Features only for long term recordings
    sdnn = np.std(nn_intervals, ddof=1)  # ddof = 1 : unbiased estimator => divide std by n-1
    cvnni = sdnn / mean_nni

    # Heart Rate equivalent features
    heart_rate_list = np.divide(60000, nn_intervals)
    mean_hr = np.mean(heart_rate_list)
    min_hr = min(heart_rate_list)
    max_hr = max(heart_rate_list)
    std_hr = np.std(heart_rate_list)

    time_domain_features = {
        'mean_nni': mean_nni,
        'sdnn': sdnn,
        'sdsd': sdsd,
        'nni_50': nni_50,
        'pnni_50': pnni_50,
        'nni_20': nni_20,
        'pnni_20': pnni_20,
        'rmssd': rmssd,
        'median_nni': median_nni,
        'range_nni': range_nni,
        'cvsd': cvsd,
        'cvnni': cvnni,
        'mean_hr': mean_hr,
        "max_hr": max_hr,
        "min_hr": min_hr,
        "std_hr": std_hr,
    }

    return time_domain_features


def get_frequency_domain_features(nn_intervals: List[float], method: str = WELCH_METHOD,
                                  sampling_frequency: int = 4, interpolation_method: str = "linear",
                                  vlf_band: namedtuple = VlfBand(0.003, 0.04),
                                  lf_band: namedtuple = LfBand(0.04, 0.15),
                                  hf_band: namedtuple = HfBand(0.15, 0.40)) -> dict:
    """
    Returns a dictionary containing frequency domain features for HRV analyses.
    To our knowledge, you might use this function on short term recordings, from 2 to 5 minutes  \
    window.
    Parameters
    ---------
    nn_intervals : list
        list of Normal to Normal Interval
    method : str
        Method used to calculate the psd. Choice are Welch's FFT or Lomb method.
    sampling_frequency : int
        Frequency at which the signal is sampled. Common value range from 1 Hz to 10 Hz,
        by default set to 4 Hz. No need to specify if Lomb method is used.
    interpolation_method : str
        kind of interpolation as a string, by default "linear". No need to specify if Lomb
        method is used.
    vlf_band : tuple
        Very low frequency bands for features extraction from power spectral density.
    lf_band : tuple
        Low frequency bands for features extraction from power spectral density.
    hf_band : tuple
        High frequency bands for features extraction from power spectral density.
    Returns
    ---------
    frequency_domain_features : dict
        Dictionary containing frequency domain features for HRV analyses. There are details
        about each features below.
    Notes
    ---------
    Details about feature engineering...
    - **total_power** : Total power density spectral
    - **vlf** : variance ( = power ) in HRV in the Very low Frequency (.003 to .04 Hz by default). \
    Reflect an intrinsic rhythm produced by the heart which is modulated primarily by sympathetic \
    activity.
    - **lf** : variance ( = power ) in HRV in the low Frequency (.04 to .15 Hz). Reflects a \
    mixture of sympathetic and parasympathetic activity, but in long-term recordings, it reflects \
    sympathetic activity and can be reduced by the beta-adrenergic antagonist propanolol.
    - **hf**: variance ( = power ) in HRV in the High Frequency (.15 to .40 Hz by default). \
    Reflects fast changes in beat-to-beat variability due to parasympathetic (vagal) activity. \
    Sometimes called the respiratory band because it corresponds to HRV changes related to the \
    respiratory cycle and can be increased by slow, deep breathing (about 6 or 7 breaths per \
    minute) and decreased by anticholinergic drugs or vagal blockade.
    - **lf_hf_ratio** : lf/hf ratio is sometimes used by some investigators as a quantitative \
    mirror of the sympatho/vagal balance.
    - **lfnu** : normalized lf power.
    - **hfnu** : normalized hf power.
    References
    ----------
    .. [1] Heart rate variability - Standards of measurement, physiological interpretation, and \
    clinical use, Task Force of The European Society of Cardiology and The North American Society \
    of Pacing and Electrophysiology, 1996
    .. [2] Signal Processing Methods for Heart Rate Variability - Gari D. Clifford, 2002
    """

    # ----------  Compute frequency & Power spectral density of signal  ---------- #
    freq, psd = _get_freq_psd_from_nn_intervals(nn_intervals=nn_intervals, method=method,
                                                sampling_frequency=sampling_frequency,
                                                interpolation_method=interpolation_method,
                                                vlf_band=vlf_band, hf_band=hf_band)

    # ---------- Features calculation ---------- #
    freqency_domain_features = _get_features_from_psd(freq=freq, psd=psd,
                                                      vlf_band=vlf_band,
                                                      lf_band=lf_band,
                                                      hf_band=hf_band)

    return freqency_domain_features

def _get_freq_psd_from_nn_intervals(nn_intervals: List[float], method: str = WELCH_METHOD,
                                    sampling_frequency: int = 4,
                                    interpolation_method: str = "linear",
                                    vlf_band: namedtuple = VlfBand(0.003, 0.04),
                                    hf_band: namedtuple = HfBand(0.15, 0.40)) -> Tuple:
    """
    Returns the frequency and power of the signal.
    Parameters
    ---------
    nn_intervals : list
        list of Normal to Normal Interval
    method : str
        Method used to calculate the psd. Choice are Welch's FFT or Lomb method.
    sampling_frequency : int
        Frequency at which the signal is sampled. Common value range from 1 Hz to 10 Hz,
        by default set to 7 Hz. No need to specify if Lomb method is used.
    interpolation_method : str
        Kind of interpolation as a string, by default "linear". No need to specify if Lomb
        method is used.
    vlf_band : tuple
        Very low frequency bands for features extraction from power spectral density.
    hf_band : tuple
        High frequency bands for features extraction from power spectral density.
    Returns
    ---------
    freq : list
        Frequency of the corresponding psd points.
    psd : list
        Power Spectral Density of the signal.
    """

    timestamp_list = _create_timestamp_list(nn_intervals)

    if method == WELCH_METHOD:
        # ---------- Interpolation of signal ---------- #
        funct = interpolate.interp1d(x=timestamp_list, y=nn_intervals, kind=interpolation_method)

        timestamps_interpolation = _create_interpolated_timestamp_list(nn_intervals, sampling_frequency)
        nni_interpolation = funct(timestamps_interpolation)

        # ---------- Remove DC Component ---------- #
        nni_normalized = nni_interpolation - np.mean(nni_interpolation)

        #  --------- Compute Power Spectral Density  --------- #
        freq, psd = signal.welch(x=nni_normalized, fs=sampling_frequency, window='hann',
                                 nfft=4096)

    else:
        raise ValueError("Not a valid method. Choose between 'lomb' and 'welch'")

    return freq, psd

def _create_interpolated_timestamp_list(nn_intervals: List[float], sampling_frequency: int = 7) -> List[float]:
    """
    Creates the interpolation time used for Fourier transform's method
    Parameters
    ---------
    nn_intervals : list
        List of Normal to Normal Interval.
    sampling_frequency : int
        Frequency at which the signal is sampled.
    Returns
    ---------
    nni_interpolation_tmstp : list
        Timestamp for interpolation.
    """
    time_nni = _create_timestamp_list(nn_intervals)
    # Create timestamp for interpolation
    nni_interpolation_tmstp = np.arange(0, time_nni[-1], 1 / float(sampling_frequency))
    return nni_interpolation_tmstp

def _get_features_from_psd(freq: List[float], psd: List[float], vlf_band: namedtuple = VlfBand(0.003, 0.04),
                           lf_band: namedtuple = LfBand(0.04, 0.15),
                           hf_band: namedtuple = HfBand(0.15, 0.40)) -> dict:
    """
    Computes frequency domain features from the power spectral decomposition.
    Parameters
    ---------
    freq : array
        Array of sample frequencies.
    psd : list
        Power spectral density or power spectrum.
    vlf_band : tuple
        Very low frequency bands for features extraction from power spectral density.
    lf_band : tuple
        Low frequency bands for features extraction from power spectral density.
    hf_band : tuple
        High frequency bands for features extraction from power spectral density.
    Returns
    ---------
    freqency_domain_features : dict
        Dictionary containing frequency domain features for HRV analyses. There are details
        about each features given below.
    """

    # Calcul of indices between desired frequency bands
    vlf_indexes = np.logical_and(freq >= vlf_band[0], freq < vlf_band[1])
    lf_indexes = np.logical_and(freq >= lf_band[0], freq < lf_band[1])
    hf_indexes = np.logical_and(freq >= hf_band[0], freq < hf_band[1])

    # Integrate using the composite trapezoidal rule
    lf = np.trapz(y=psd[lf_indexes], x=freq[lf_indexes])
    hf = np.trapz(y=psd[hf_indexes], x=freq[hf_indexes])

    # total power & vlf : Feature often used for  "long term recordings" analysis
    vlf = np.trapz(y=psd[vlf_indexes], x=freq[vlf_indexes])
    total_power = vlf + lf + hf

    lf_hf_ratio = lf / hf
    lfnu = (lf / (lf + hf)) * 100
    hfnu = (hf / (lf + hf)) * 100

    freqency_domain_features = {
        'lf': lf,
        'hf': hf,
        'lf_hf_ratio': lf_hf_ratio,
        'lfnu': lfnu,
        'hfnu': hfnu,
        'total_power': total_power,
        'vlf': vlf
    }

    return freqency_domain_features

def _create_timestamp_list(nn_intervals: List[float]) -> List[float]:
    """
    Creates corresponding time interval for all nn_intervals
    Parameters
    ---------
    nn_intervals : list
        List of Normal to Normal Interval.
    Returns
    ---------
    nni_tmstp : list
        list of time intervals between first NN-interval and final NN-interval.
    """
    # Convert in seconds
    nni_tmstp = np.cumsum(nn_intervals) / 1000

    # Force to start at 0
    return nni_tmstp - nni_tmstp[0]