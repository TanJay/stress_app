package com.tanushaj.element.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.HighLowDataEntry;
import com.anychart.charts.Stock;
import com.anychart.core.stock.Plot;
import com.anychart.data.Table;
import com.anychart.data.TableMapping;
import com.anychart.enums.StockSeriesType;
import com.tanushaj.element.R;
import com.tanushaj.element.SessionViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SessionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionFragment extends Fragment{
    AnyChartView anyChartView;

    private OnFragmentInteractionListener mListener;

    public SessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SessionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionFragment newInstance() {
        SessionFragment fragment = new SessionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session, container, false);










        anyChartView = view.findViewById(R.id.histogramChart);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));
        Table table = Table.instantiate("x");
        table.addData(getData());

        TableMapping mapping = table.mapAs("{open: 'open', high: 'high', low: 'low', close: 'close'}");

        Stock stock = AnyChart.stock();

        Plot plot = stock.plot(0);
        plot.yGrid(true)
                .xGrid(true)
                .yMinorGrid(true)
                .xMinorGrid(true);

        plot.ema(table.mapAs("{value: 'close'}"), 20d, StockSeriesType.LINE);

        plot.ohlc(mapping)
                .name("CSCO")
                .legendItem("{\n" +
                        "        iconType: 'rising-falling'\n" +
                        "      }");

        stock.scroller().ohlc(mapping);
        anyChartView.setChart(stock);
        return  view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private List<DataEntry> getData() {
        List<DataEntry> data = new ArrayList<>();
        data.add(new SessionFragment.OHCLDataEntry(638380800000L,0.0825,0.0842,0.0816,0.0842));
        data.add(new SessionFragment.OHCLDataEntry(638467200000L,0.0833,0.0859,0.0825,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(638553600000L,0.0859,0.0859,0.0851,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(638640000000L,0.0851,0.0851,0.0833,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(638726400000L,0.0833,0.0833,0.0799,0.0816));
        data.add(new SessionFragment.OHCLDataEntry(638985600000L,0.079,0.0807,0.0773,0.0799));
        data.add(new SessionFragment.OHCLDataEntry(639072000000L,0.079,0.0816,0.079,0.0807));
        data.add(new SessionFragment.OHCLDataEntry(639158400000L,0.0807,0.0816,0.0807,0.0807));
        data.add(new SessionFragment.OHCLDataEntry(639244800000L,0.0816,0.0816,0.0799,0.0803));
        data.add(new SessionFragment.OHCLDataEntry(639331200000L,0.0799,0.0807,0.079,0.079));
        data.add(new SessionFragment.OHCLDataEntry(639590400000L,0.0799,0.0799,0.0781,0.0781));
        data.add(new SessionFragment.OHCLDataEntry(639676800000L,0.0781,0.0799,0.0746,0.0764));
        data.add(new SessionFragment.OHCLDataEntry(639763200000L,0.0764,0.0773,0.0755,0.0764));
        data.add(new SessionFragment.OHCLDataEntry(639849600000L,0.0773,0.0773,0.0755,0.0773));
        data.add(new SessionFragment.OHCLDataEntry(640195200000L,0.0773,0.0781,0.0764,0.0773));
        data.add(new SessionFragment.OHCLDataEntry(640281600000L,0.0764,0.0781,0.0755,0.0781));
        data.add(new SessionFragment.OHCLDataEntry(640368000000L,0.0781,0.0799,0.0773,0.079));
        data.add(new SessionFragment.OHCLDataEntry(640454400000L,0.0781,0.0799,0.0781,0.0799));
        data.add(new SessionFragment.OHCLDataEntry(640540800000L,0.0799,0.0799,0.0773,0.0773));
        data.add(new SessionFragment.OHCLDataEntry(640800000000L,0.0773,0.079,0.0764,0.0781));
        data.add(new SessionFragment.OHCLDataEntry(640886400000L,0.0799,0.0799,0.079,0.0794));
        data.add(new SessionFragment.OHCLDataEntry(640972800000L,0.0799,0.0807,0.0799,0.0799));
        data.add(new SessionFragment.OHCLDataEntry(641059200000L,0.0799,0.0816,0.0799,0.0799));
        data.add(new SessionFragment.OHCLDataEntry(641145600000L,0.0799,0.0807,0.079,0.079));
        data.add(new SessionFragment.OHCLDataEntry(641404800000L,0.0807,0.0842,0.0799,0.0825));
        data.add(new SessionFragment.OHCLDataEntry(641491200000L,0.0842,0.0842,0.0825,0.0838));
        data.add(new SessionFragment.OHCLDataEntry(641577600000L,0.0833,0.0842,0.0825,0.0825));
        data.add(new SessionFragment.OHCLDataEntry(641664000000L,0.0842,0.0851,0.0833,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(641750400000L,0.0842,0.0859,0.0842,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(642009600000L,0.0859,0.0885,0.0859,0.0868));
        data.add(new SessionFragment.OHCLDataEntry(642096000000L,0.0885,0.0885,0.0859,0.0877));
        data.add(new SessionFragment.OHCLDataEntry(642182400000L,0.0877,0.0877,0.0851,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(642268800000L,0.0868,0.0868,0.0851,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(642355200000L,0.0885,0.0894,0.0851,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(642614400000L,0.0877,0.0877,0.0859,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(642700800000L,0.0877,0.0877,0.0859,0.0864));
        data.add(new SessionFragment.OHCLDataEntry(642787200000L,0.0877,0.0877,0.0851,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(642873600000L,0.0868,0.0868,0.0859,0.0868));
        data.add(new SessionFragment.OHCLDataEntry(642960000000L,0.0859,0.0868,0.0842,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(643219200000L,0.0842,0.0868,0.0842,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(643305600000L,0.0851,0.0859,0.0851,0.0851));
        data.add(new SessionFragment.OHCLDataEntry(643392000000L,0.0851,0.0859,0.0842,0.0842));
        data.add(new SessionFragment.OHCLDataEntry(643478400000L,0.0859,0.0859,0.0842,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(643564800000L,0.0833,0.0851,0.0825,0.0833));
        data.add(new SessionFragment.OHCLDataEntry(643910400000L,0.0833,0.0851,0.0816,0.0825));
        data.add(new SessionFragment.OHCLDataEntry(643996800000L,0.0833,0.0833,0.0807,0.0816));
        data.add(new SessionFragment.OHCLDataEntry(644083200000L,0.0807,0.0868,0.0807,0.0859));
        data.add(new SessionFragment.OHCLDataEntry(644169600000L,0.0859,0.0885,0.0851,0.0868));
        data.add(new SessionFragment.OHCLDataEntry(644428800000L,0.0877,0.0894,0.0859,0.0894));
        data.add(new SessionFragment.OHCLDataEntry(644515200000L,0.0885,0.0938,0.0885,0.0929));
        data.add(new SessionFragment.OHCLDataEntry(644601600000L,0.0929,0.0929,0.0903,0.092));
        data.add(new SessionFragment.OHCLDataEntry(644688000000L,0.0929,0.0929,0.0885,0.0894));
        data.add(new SessionFragment.OHCLDataEntry(644774400000L,0.0903,0.0903,0.0868,0.0877));
        data.add(new SessionFragment.OHCLDataEntry(645033600000L,0.0851,0.0877,0.0851,0.0872));
        data.add(new SessionFragment.OHCLDataEntry(645120000000L,0.0877,0.0972,0.0868,0.0955));
        data.add(new SessionFragment.OHCLDataEntry(645206400000L,0.0972,0.0972,0.0963,0.0963));
        data.add(new SessionFragment.OHCLDataEntry(645292800000L,0.0972,0.0981,0.0955,0.0972));
        data.add(new SessionFragment.OHCLDataEntry(645379200000L,0.0955,0.0972,0.0955,0.0955));

        return data;
    }

    private class OHCLDataEntry extends HighLowDataEntry {
        OHCLDataEntry(Long x, Double open, Double high, Double low, Double close) {
            super(x, high, low);
            setValue("open", open);
            setValue("close", close);
        }
    }
}
