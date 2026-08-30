import { useEffect, useState } from "react";
import { Alert, Button, Card } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis  } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const LessonDetail = () => {

    const { lessonId } = useParams();
    const nav = useNavigate();

    const [lesson,setLesson] = useState(null);
    const [loading,setLoading] = useState(true);
    const [err,setErr] = useState("");


    const loadLesson = async () => {

        try {

            setLoading(true);
            setErr("");

            const res = await authApis()
                .get(`/student/lessons/${lessonId}`);

            setLesson(res.data);

        } catch(ex) {

            console.error(ex);

            setErr(
                ex.response?.data?.message ||
                "Không thể tải bài học!"
            );

        } finally {

            setLoading(false);

        }

    };


    useEffect(()=>{
        loadLesson();
    },[lessonId]);


    if(loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5">
                    <MySpinner/>
                </div>
            </div>
        );


    if(err)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container">

                    <Alert variant="danger">
                        {err}
                    </Alert>

                    <Button
                        variant="outline-secondary"
                        onClick={()=>nav(-1)}
                    >
                        Quay lại
                    </Button>

                </div>
            </div>
        );


    return (

        <div className="cm-portal-page">

            <div className="cm-portal-container">


                <Button
                    size="sm"
                    variant="outline-secondary"
                    className="mb-3"
                    onClick={()=>nav(-1)}
                >
                    ← Quay lại
                </Button>


                <Card className="cm-portal-card">

                    <Card.Body className="p-4">


                        <span className="cm-portal-label">
                            BÀI HỌC
                        </span>


                        <h2 className="cm-portal-title mt-2">
                            {lesson.lessonName}
                        </h2>


                        <div className="mt-4">

                            {
                                lesson.fileUrl ? (

                                    <iframe
                                        src={lesson.fileUrl}
                                        title={lesson.lessonName}
                                        width="100%"
                                        height="700px"
                                        style={{
                                            border:"1px solid #ddd",
                                            borderRadius:"10px"
                                        }}
                                    />

                                    
                                    

                                ) : (

                                    <Alert variant="warning">
                                        Bài học chưa có tài liệu.
                                    </Alert>

                                )
                            }


                        </div>

                    </Card.Body>

                </Card>


            </div>

        </div>

    );

};


export default LessonDetail;