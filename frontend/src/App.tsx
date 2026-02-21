import HomePage from "./pages/HomePage/HomePage"
import { Route, Routes } from "react-router-dom"
import JobPage from "./pages/JobPage/JobPage"

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/jobs/:jobId" element={<JobPage />} />
    </Routes>
  )
}