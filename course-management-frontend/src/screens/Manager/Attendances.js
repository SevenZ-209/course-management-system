import AttendanceManagement from "../../components/AttendanceManagement";
import { endpoints } from "../../configs/Apis";

const Attendances = () => (
    <AttendanceManagement
        attendanceEndpoint={endpoints.managerAttendances}
        courseOptionsEndpoint={endpoints.managerCourseOptions}
        classOptionsEndpoint={endpoints.managerClassOptions}
        sessionOptionsEndpoint={endpoints.managerOnlineSessionOptions}
    />
);

export default Attendances;
