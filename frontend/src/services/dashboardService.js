import api from "./api.js";

const dashboardService = {
  async getSummary() {
    const response = await api.get("/dashboard/summary");
    return response.data;
  },

  async getUpcomingBills() {
    const response = await api.get("/dashboard/upcoming-bills");
    return response.data;
  },

  async getChartData() {
    const response = await api.get("/dashboard/charts");
    return response.data;
  },
};

export default dashboardService;
