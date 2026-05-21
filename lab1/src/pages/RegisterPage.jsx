import { useState } from 'react';
import { UserPlus } from 'lucide-react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import { registerUser } from '../store/authSlice.js';

const initialForm = {
  name: '',
  email: '',
  gender: 'Жінка',
  birthDate: '',
  password: '',
};

export default function RegisterPage() {
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();

    if (!form.name.trim() || !form.email.trim() || !form.birthDate || form.password.length < 4) {
      setError('Заповніть усі поля. Пароль має містити щонайменше 4 символи.');
      return;
    }

    dispatch(registerUser({ ...form, name: form.name.trim(), email: form.email.trim().toLowerCase() }));
    navigate('/profile');
  }

  return (
    <section className="container page-grid auth-page">
      <PageHeader eyebrow="Обліковий запис" title="Реєстрація користувача">
        Створіть профіль для роботи з опитуваннями та збереженням даних у браузері.
      </PageHeader>

      <form className="surface-form" onSubmit={handleSubmit}>
        {error ? <div className="alert alert-danger">{error}</div> : null}

        <label className="form-label" htmlFor="name">
          Ім&apos;я
        </label>
        <input
          className="form-control"
          id="name"
          name="name"
          type="text"
          value={form.name}
          onChange={updateField}
          placeholder="Олена Коваль"
        />

        <label className="form-label" htmlFor="email">
          Email
        </label>
        <input
          className="form-control"
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={updateField}
          placeholder="student@example.com"
        />

        <label className="form-label" htmlFor="gender">
          Стать
        </label>
        <select className="form-select" id="gender" name="gender" value={form.gender} onChange={updateField}>
          <option>Жінка</option>
          <option>Чоловік</option>
          <option>Не вказувати</option>
        </select>

        <label className="form-label" htmlFor="birthDate">
          Дата народження
        </label>
        <input
          className="form-control"
          id="birthDate"
          name="birthDate"
          type="date"
          value={form.birthDate}
          onChange={updateField}
        />

        <label className="form-label" htmlFor="password">
          Пароль
        </label>
        <input
          className="form-control"
          id="password"
          name="password"
          type="password"
          value={form.password}
          onChange={updateField}
          placeholder="Мінімум 4 символи"
        />

        <button className="btn btn-dark btn-lg w-100" type="submit">
          <UserPlus size={18} aria-hidden="true" />
          Зареєструватись
        </button>
      </form>
    </section>
  );
}
