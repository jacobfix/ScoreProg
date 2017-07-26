package jacobfix.scorepredictor;

import jacobfix.scorepredictor.schedule.ScheduleRetriever;

public class RetrieverFactory {

    // private DetailsRetriever detailsRetriever = new DetailsRetriever();
    private ScheduleRetriever scheduleRetriever = new ScheduleRetriever();

    public ScheduleRetriever getScheduleRetriever() {
        return scheduleRetriever;
    }
}
