import { useEffect, useState } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";

const AnswerModal = ({show, onHide, question}) => {

    const [answers,setAnswers] = useState([]);
    const [loading,setLoading] = useState(false);
    const [saving,setSaving] = useState(false);
    const [err,setErr] = useState("");

    const loadAnswers = async () => {
        if(!question) return;

        try{
            setLoading(true);

            const res = await authApis().get(
                `${endpoints.adminAnswers}?questionId=${question.id}`
            );

            setAnswers(
                Array.isArray(res.data)
                ? res.data
                : res.data.answers || []
            );

        }catch(ex){
            setErr(
                ex.response?.data?.message ||
                "Không tải được đáp án!"
            );
        }finally{
            setLoading(false);
        }
    };

    useEffect(()=>{
        if(show)
            loadAnswers();
    },[show,question]);

    const addAnswer = () => {

        setAnswers([
            ...answers,
            {
                content:"",
                correct:false,
                orderNumber:answers.length + 1
            }
        ]);

    };

    const changeAnswer = (index,value) => {

        const list=[...answers];

        list[index].content=value;

        setAnswers(list);

    };

    const selectCorrect = index => {

        setAnswers(
            answers.map((a,i)=>({
                ...a,
                correct:i===index
            }))
        );

    };

    const removeAnswer = index => {

        setAnswers(
            answers
            .filter((_,i)=>i!==index)
            .map((a,i)=>({
                ...a,
                orderNumber:i+1
            }))
        );

    };

    const save = async()=>{

        const correctCount =
            answers.filter(a=>a.correct).length;

        if(correctCount !== 1)
            return setErr(
                "Câu hỏi trắc nghiệm phải có đúng 1 đáp án đúng!"
            );

        if(
            answers.some(a=>!a.content.trim())
        )
            return setErr(
                "Nội dung đáp án không được để trống!"
            );

        try{

            setSaving(true);
            setErr("");

            await authApis().post(
                endpoints.adminAnswersBulk,
                {
                    questionId:question.id,
                    answers:answers.map((a,i)=>({
                        content:a.content.trim(),
                        correct:a.correct,
                        orderNumber:i+1
                    }))
                }
            );

            onHide();

        }catch(ex){

            setErr(
                ex.response?.data?.message ||
                "Lưu đáp án thất bại!"
            );

        }finally{

            setSaving(false);

        }

    };

    return (

        <Modal
            show={show}
            onHide={onHide}
            centered
        >

            <Modal.Header closeButton>

                <Modal.Title>
                    Quản lý đáp án
                </Modal.Title>

            </Modal.Header>

            <Modal.Body>

                <p className="fw-semibold">
                    {question?.content}
                </p>

                {err && (
                    <Alert
                        variant="danger"
                        onClose={()=>setErr("")}
                        dismissible
                    >
                        {err}
                    </Alert>
                )}

                {
                    loading ?

                    <div>
                        Đang tải...
                    </div>

                    :

                    answers.map((a,index)=>(

                        <div
                            key={index}
                            className="d-flex gap-2 mb-3 align-items-center"
                        >

                            <Form.Check
                                type="radio"
                                checked={a.correct}
                                onChange={()=>selectCorrect(index)}
                            />

                            <Form.Control
                                value={a.content}
                                placeholder={`Đáp án ${index+1}`}
                                onChange={e=>
                                    changeAnswer(
                                        index,
                                        e.target.value
                                    )
                                }
                            />

                            <Button
                                variant="outline-danger"
                                size="sm"
                                onClick={()=>
                                    removeAnswer(index)
                                }
                            >
                                X
                            </Button>

                        </div>

                    ))

                }

                <Button
                    variant="outline-success"
                    onClick={addAnswer}
                >
                    + Thêm đáp án
                </Button>

            </Modal.Body>

            <Modal.Footer>

                <Button
                    variant="secondary"
                    onClick={onHide}
                >
                    Đóng
                </Button>

                <Button
                    disabled={saving}
                    onClick={save}
                >
                    {
                        saving
                        ?"Đang lưu..."
                        :"Lưu đáp án"
                    }
                </Button>

            </Modal.Footer>

        </Modal>

    );

};

export default AnswerModal;