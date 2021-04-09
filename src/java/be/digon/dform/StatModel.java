/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author rombouts
 */
@Named
@ViewScoped
public class StatModel implements Serializable {

    @Inject
    private DataConnector dataConnector;

    @PostConstruct
    public void refreshStats() {
        createSegmentPatientStats();
        createSubPerTypeStats();
    }

    private void createSegmentPatientStats() {
        segmentPatientStats = new PieChartModel();
        int patientCount = 0;
        try {
            Map<String, BigDecimal> segmentKeyVals = dataConnector.getSegmentPatientStatData();
            for (String lbl : segmentKeyVals.keySet()) {
                segmentPatientStats.set(lbl, segmentKeyVals.get(lbl));
                patientCount += segmentKeyVals.get(lbl).intValue();
            }
            segmentPatientStats.setTitle("Patients per center");
            segmentPatientStats.setLegendPosition("e");
            segmentPatientStats.setShowDataLabels(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nrPatients = patientCount;
    }
    
    private void createSubPerTypeStats(){
        subPerTypeStats = new BarChartModel();
        int nrFinalSubmissionsCount = 0;
        int nrDraftSubmissionsCount = 0;
        ChartSeries finalSub = new ChartSeries();
        ChartSeries draftSub = new ChartSeries();
        
         try {
            Map<String, BigDecimal> subPerTpMapDraft = dataConnector.getSubPerTypeStatData(true);
            Map<String, BigDecimal> subPerTpMapFinal = dataConnector.getSubPerTypeStatData(false);
            Set<String> labels = new TreeSet();
            labels.addAll(subPerTpMapDraft.keySet());
            labels.addAll(subPerTpMapFinal.keySet());
            
            for (String lbl : labels) {
                finalSub.set(lbl, subPerTpMapFinal.containsKey(lbl) ? subPerTpMapFinal.get(lbl) : BigDecimal.ZERO);
                draftSub.set(lbl, subPerTpMapDraft.containsKey(lbl) ? subPerTpMapDraft.get(lbl) : BigDecimal.ZERO);
                if (subPerTpMapFinal.containsKey(lbl)){
                    nrFinalSubmissionsCount += subPerTpMapFinal.get(lbl).intValue();
                }
                if (subPerTpMapDraft.containsKey(lbl)){
                    nrDraftSubmissionsCount += subPerTpMapDraft.get(lbl).intValue();
                }
            }
            finalSub.setLabel("Final submissions");
            draftSub.setLabel("Draft submissions");
            subPerTypeStats.addSeries(finalSub);
            subPerTypeStats.addSeries(draftSub);
            subPerTypeStats.setLegendPosition("e");
            subPerTypeStats.setTitle("Submissions per instance");
            subPerTypeStats.setStacked(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
         nrFinalSubmissions= nrFinalSubmissionsCount;
         nrDraftSubmissions = nrDraftSubmissionsCount;
    }

    private PieChartModel segmentPatientStats;

    public PieChartModel getSegmentPatientStats() {
        return segmentPatientStats;
    }

    public void setSegmentPatientStats(PieChartModel segmentPatientStats) {
        this.segmentPatientStats = segmentPatientStats;
    }

    private BarChartModel subPerTypeStats;

    public BarChartModel getSubPerTypeStats() {
        return subPerTypeStats;
    }

    public void setSubPerTypeStats(BarChartModel subPerTypeStats) {
        this.subPerTypeStats = subPerTypeStats;
    }
    
    
    private Integer nrPatients;

    public Integer getNrPatients() {
        return nrPatients;
    }

    public void setNrPatients(Integer nrPatients) {
        this.nrPatients = nrPatients;
    }

    private Integer nrFinalSubmissions;

    public Integer getNrFinalSubmissions() {
        return nrFinalSubmissions;
    }

    public void setNrFinalSubmissions(Integer nrFinalSubmissions) {
        this.nrFinalSubmissions = nrFinalSubmissions;
    }
    
    
    private Integer nrDraftSubmissions;

    public Integer getNrDraftSubmissions() {
        return nrDraftSubmissions;
    }

    public void setNrDraftSubmissions(Integer nrDraftSubmissions) {
        this.nrDraftSubmissions = nrDraftSubmissions;
    }
    
}
