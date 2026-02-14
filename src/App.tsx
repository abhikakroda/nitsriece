import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Schedule from "./pages/Schedule";
import Profile from "./pages/Profile";
import AttendanceAnalytics from "./pages/AttendanceAnalytics";
import MessMenu from "./pages/MessMenu";
import NotFound from "./pages/NotFound";
import BottomNav from "./components/BottomNav";

const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<Dashboard />} />
    <Route path="/schedule" element={<Schedule />} />
    <Route path="/analytics" element={<AttendanceAnalytics />} />
    <Route path="/mess-menu" element={<MessMenu />} />
    <Route path="/profile" element={<Profile />} />
    <Route path="*" element={<NotFound />} />
  </Routes>
);

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <div className="lg:pl-[220px]">
          <AppRoutes />
        </div>
        <BottomNav />
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
