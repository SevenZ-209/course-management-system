import { Button, Form, Modal } from "react-bootstrap";

const QuestionModal = ({
    show,
    close,
    save,
    saving,
    editing,
    form,
    setForm,
    assignments,
    assignmentId
}) => {

    return (
        <Modal
            show={show}
            onHide={close}
            centered
        >

            <Form onSubmit={save}>

                <Modal.Header closeButton>

                    <Modal.Title>
                        {
                            editing
                            ? "Cập nhật câu hỏi"
                            : "Thêm câu hỏi"
                        }
                    </Modal.Title>

                </Modal.Header>


                <Modal.Body>


                    {
                        !assignmentId && (

                            <Form.Group className="mb-3">

                                <Form.Label>
                                    Bài tập
                                </Form.Label>


                                <Form.Select
                                    value={form.assignmentId}
                                    onChange={e =>
                                        setForm({
                                            ...form,
                                            assignmentId:e.target.value
                                        })
                                    }
                                >

                                    <option value="">
                                        -- Chọn bài tập --
                                    </option>


                                    {
                                        assignments.map(a=>(

                                            <option
                                                key={a.id}
                                                value={a.id}
                                            >
                                                {a.name}
                                            </option>

                                        ))
                                    }

                                </Form.Select>

                            </Form.Group>

                        )
                    }



                    <Form.Group className="mb-3">

                        <Form.Label>
                            Nội dung câu hỏi
                        </Form.Label>


                        <Form.Control
                            as="textarea"
                            rows={3}
                            value={form.content}
                            onChange={e =>
                                setForm({
                                    ...form,
                                    content:e.target.value
                                })
                            }
                            placeholder="Nhập nội dung câu hỏi"
                        />

                    </Form.Group>



                    <Form.Group className="mb-3">

                        <Form.Label>
                            Loại câu hỏi
                        </Form.Label>


                        <Form.Select
                            value={form.type}
                            onChange={e =>
                                setForm({
                                    ...form,
                                    type:e.target.value
                                })
                            }
                        >

                            <option value="MULTIPLE_CHOICE">
                                Trắc nghiệm
                            </option>


                            <option value="ESSAY">
                                Tự luận
                            </option>


                        </Form.Select>

                    </Form.Group>




                    <Form.Group className="mb-3">

                        <Form.Label>
                            Điểm
                        </Form.Label>


                        <Form.Control
                            type="number"
                            min="1"
                            value={form.score}
                            onChange={e =>
                                setForm({
                                    ...form,
                                    score:e.target.value
                                })
                            }
                        />

                    </Form.Group>



                    <Form.Group>

                        <Form.Label>
                            Thứ tự
                        </Form.Label>


                        <Form.Control
                            type="number"
                            min="1"
                            value={form.orderNumber}
                            onChange={e =>
                                setForm({
                                    ...form,
                                    orderNumber:e.target.value
                                })
                            }
                        />

                    </Form.Group>


                </Modal.Body>



                <Modal.Footer>

                    <Button
                        variant="secondary"
                        onClick={close}
                        disabled={saving}
                    >
                        Hủy
                    </Button>


                    <Button
                        type="submit"
                        disabled={saving}
                    >
                        {
                            saving
                            ? "Đang lưu..."
                            : "Lưu"
                        }
                    </Button>


                </Modal.Footer>


            </Form>

        </Modal>
    );
};


export default QuestionModal;