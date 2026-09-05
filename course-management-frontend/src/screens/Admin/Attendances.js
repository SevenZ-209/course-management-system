import AttendanceManagement from "../../components/AttendanceManagement";
import { endpoints } from "../../configs/Apis";

const Attendances = () => (
    <AttendanceManagement
        attendanceEndpoint={endpoints.adminAttendances}
        courseOptionsEndpoint={endpoints.adminCourseOptions}
        classOptionsEndpoint={endpoints.adminClassOptions}
        sessionOptionsEndpoint={endpoints.adminOnlineSessionOptions}
    />
);

export default Attendances;
