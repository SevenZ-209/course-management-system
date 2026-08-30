import { useContext } from "react";
import { Navigate, useLocation } from "react-router-dom";

import { MyUserContext } from "../configs/Contexts";

const ProtectedRoute = ({ children, roles }) => {
    const [user] = useContext(MyUserContext);
    const location = useLocation();

    if (!user) {
        return (
            <Navigate
                to={`/login?next=${encodeURIComponent(location.pathname)}`}
                replace
            />
        );
    }

    if (roles && !roles.includes(user.role)) {
        return <Navigate to="/unauthorized" replace />;
    }

    return children;
};

export default ProtectedRoute;