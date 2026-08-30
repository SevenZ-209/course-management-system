const MyUserReducer = (current, action) => {

    switch (action.type) {

        case "LOGIN":
            return action.payload;

        case "LOGOUT":
            return null;

        default:
            return current;
    }
};


export default MyUserReducer;