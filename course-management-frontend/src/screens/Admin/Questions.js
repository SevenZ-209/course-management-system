import { useEffect, useState } from "react";
import { Alert, Button, Card, Pagination, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import QuestionModal from "../../components/QuestionModal";
import AnswerModal from "../../components/AnswerModal";

const Questions = () => {
    const [q,setQ] = useSearchParams();

    const [questions,setQuestions] = useState([]);
    const [assignments,setAssignments] = useState([]);

    const [loading,setLoading] = useState(false);
    const [saving,setSaving] = useState(false);

    const [err,setErr] = useState("");
    const [success,setSuccess] = useState("");

    const [showModal,setShowModal] = useState(false);
    const [editing,setEditing] = useState(null);

    const [showAnswer,setShowAnswer] = useState(false);
    const [selectedQuestion,setSelectedQuestion] = useState(null);

    const [totalPages,setTotalPages] = useState(1);
    const [totalRecords,setTotalRecords] = useState(0);

    const [form,setForm] = useState({
        assignmentId:"",
        content:"",
        type:"MULTIPLE_CHOICE",
        score:"",
        orderNumber:""
    });

    const page = Number(q.get("page")) || 1;
    const assignmentId = q.get("assignmentId") || "";

    const loadQuestions = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = {page};

            if(assignmentId)
                params.assignmentId = assignmentId;

            const res = await authApis().get(
                endpoints.adminQuestions,
                {params}
            );

            const data = res.data;

            setQuestions(
                data.questions ||
                data.content ||
                []
            );

            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);

        } catch(ex){
            setErr(
                ex.response?.data?.message ||
                "Không thể tải câu hỏi!"
            );
        } finally {
            setLoading(false);
        }
    };

    const loadAssignments = async () => {
        try {
    
            const res = await authApis().get(
                endpoints.adminAssignmentOptions
            );
    
            const data = res.data;
    
            setAssignments(
                data.assignments ||
                data.content ||
                data.items ||
                data ||
                []
            );
    
        } catch(ex){
        }
    };

    useEffect(()=>{
        loadAssignments();
    },[]);

    useEffect(()=>{
        loadQuestions();
    },[q]);

    const openAdd = () => {

        setEditing(null);

        setForm({
            assignmentId:assignmentId || "",
            content:"",
            type:"MULTIPLE_CHOICE",
            score:"",
            orderNumber:""
        });

        setShowModal(true);
    };

    const openEdit = item => {

        setEditing(item);

        setForm({
            assignmentId:item.assignmentId ?? item.assignment?.id ?? "",
            content:item.content || "",
            type:item.type || "MULTIPLE_CHOICE",
            score:item.score ?? "",
            orderNumber:item.orderNumber ?? ""
        });

        setShowModal(true);
    };

    const saveQuestion = async e => {

        e.preventDefault();

        if(!form.content.trim())
            return setErr("Vui lòng nhập nội dung câu hỏi!");

        if(!form.assignmentId)
            return setErr("Vui lòng chọn bài tập!");

        try {

            setSaving(true);
            setErr("");

            const body = {
                assignmentId:Number(form.assignmentId),
                content:form.content.trim(),
                type:form.type,
                score:Number(form.score),
                orderNumber:Number(form.orderNumber)
            };

            if(editing)

                await authApis().put(
                    `${endpoints.adminQuestions}/${editing.id}`,
                    body
                );

            else

                await authApis().post(
                    endpoints.adminQuestions,
                    body
                );

            setSuccess(
                editing
                ? "Cập nhật câu hỏi thành công!"
                : "Thêm câu hỏi thành công!"
            );

            setShowModal(false);
            loadQuestions();

        } catch(ex){

            setErr(
                ex.response?.data?.message ||
                "Lưu câu hỏi thất bại!"
            );

        } finally {

            setSaving(false);

        }

    };

    const assignmentName = item => {

        const id =
            item.assignmentId ??
            item.assignment?.id;

        return assignments.find(
            a=>String(a.id)===String(id)
        )?.name || "-";
    };

    const changePage = p => {

        const params = Object.fromEntries(q);

        params.page = p;

        setQ(params);
    };

    return (
        <>

            <Button
                size="sm"
                variant="outline-secondary"
                className="mb-3"
                onClick={()=>window.history.back()}
            >
                ← Quay lại
            </Button>

            <div className="d-flex justify-content-between align-items-center mb-4">

                <div>

                    <h2 className="fw-bold mb-1">
                        Quản lý câu hỏi
                    </h2>

                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> câu hỏi
                    </p>

                </div>

                <Button onClick={openAdd}>
                    + Thêm câu hỏi
                </Button>

            </div>

            {success &&
                <Alert variant="success" dismissible onClose={()=>setSuccess("")}>
                    {success}
                </Alert>
            }

            {err &&
                <Alert variant="danger" dismissible onClose={()=>setErr("")}>
                    {err}
                </Alert>
            }

            <Card className="border-0 shadow-sm">

                <Card.Body className="p-0">

                    {
                        loading ?

                        <div className="text-center p-5">
                            <MySpinner/>
                        </div>

                        :

                        <Table hover className="align-middle mb-0">

                            <thead className="table-light">

                                <tr>
                                    <th>ID</th>
                                    <th>Nội dung</th>
                                    <th>Bài tập</th>
                                    <th>Loại</th>
                                    <th>Điểm</th>
                                    <th>Thứ tự</th>
                                    <th>Thao tác</th>
                                </tr>

                            </thead>

                            <tbody>

                                {
                                    questions.map(item=>(

                                        <tr key={item.id}>

                                            <td>{item.id}</td>

                                            <td className="fw-semibold">
                                                {item.content}
                                            </td>

                                            <td>
                                                {assignmentName(item)}
                                            </td>

                                            <td>
                                                {item.type}
                                            </td>

                                            <td>
                                                {item.score}
                                            </td>

                                            <td>
                                                {item.orderNumber}
                                            </td>

                                            <td>

                                                {
                                                    item.type==="MULTIPLE_CHOICE" &&
                                                    <Button
                                                        size="sm"
                                                        variant="success"
                                                        className="me-2"
                                                        onClick={()=>{
                                                            setSelectedQuestion(item);
                                                            setShowAnswer(true);
                                                        }}
                                                    >
                                                        Đáp án
                                                    </Button>
                                                }

                                                <Button
                                                    size="sm"
                                                    variant="outline-primary"
                                                    onClick={()=>openEdit(item)}
                                                >
                                                    Sửa
                                                </Button>

                                            </td>

                                        </tr>

                                    ))
                                }

                            </tbody>

                        </Table>

                    }

                </Card.Body>

            </Card>

            {
                totalPages > 1 &&

                <div className="d-flex justify-content-center mt-4">

                    <Pagination>

                        <Pagination.First
                            disabled={page===1}
                            onClick={()=>changePage(1)}
                        />

                        <Pagination.Prev
                            disabled={page===1}
                            onClick={()=>changePage(page-1)}
                        />

                        {
                            Array.from(
                                {length:totalPages},
                                (_,i)=>i+1
                            ).map(i=>

                                <Pagination.Item
                                    key={i}
                                    active={page===i}
                                    onClick={()=>changePage(i)}
                                >
                                    {i}
                                </Pagination.Item>

                            )
                        }

                        <Pagination.Next
                            disabled={page===totalPages}
                            onClick={()=>changePage(page+1)}
                        />

                        <Pagination.Last
                            disabled={page===totalPages}
                            onClick={()=>changePage(totalPages)}
                        />

                    </Pagination>

                </div>
            }

            <QuestionModal
                show={showModal}
                close={()=>setShowModal(false)}
                save={saveQuestion}
                saving={saving}
                editing={editing}
                form={form}
                setForm={setForm}
                assignments={assignments}
                assignmentId={assignmentId}
            />

            <AnswerModal
                show={showAnswer}
                question={selectedQuestion}
                onHide={()=>{
                    setShowAnswer(false);
                    setSelectedQuestion(null);
                }}
            />

        </>
    );
};

export default Questions;