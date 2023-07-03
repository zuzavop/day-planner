package com.myplanner.app;

import org.jetbrains.annotations.NotNull;

/**
 * abstract class for connection to database
 */
interface Connection {
    /**
     * get plans from database
     *
     * @param name  name of request plans
     * @param year  year of request plans
     * @param month month of request plans
     * @param day   day of request plans
     * @return plans which correspond to request (given parameters)
     */
    Plan[] getPlans(@NotNull String name, int year, int month, int day) throws ConnectionException;

    /**
     * get plans on given date
     *
     * @param y year of request plans
     * @param m month of request plans
     * @param d day of request plans
     * @return plans that match given date
     */
    Plan[] getPlansOnDay(int y, int m, int d) throws ConnectionException;

    /**
     * get years that are present in database
     *
     * @return array of sort years from database
     */
    String[] getYears() throws ConnectionException;

    /**
     * add new plan to database
     *
     * @param item new set plan
     */
    void addNew(@NotNull Plan item) throws ConnectionException ;

    /**
     * update plan in database according to given plan
     *
     * @param item plan that was changed (must be update in database)
     */
    void updatePlan(@NotNull Plan item) throws ConnectionException ;

    /**
     * delete given plan from database
     *
     * @param idPlan plan which should be delete
     */
    void deletePlan(int idPlan) throws ConnectionException ;
}

class ConnectionException extends Exception {
    public ConnectionException(String message) {
        super(message);
    }
}