import { useEffect, useMemo, useState } from 'react';
import { Plus, Send, Trash2, Vote } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import PageHeader from '../components/PageHeader.jsx';
import { addOption, addPoll, fetchRemotePolls, removePoll, vote } from '../store/pollsSlice.js';
import { getPollTotalVotes } from '../utils/selectors.js';

const defaultPoll = {
  title: '',
  description: '',
  optionA: '',
  optionB: '',
};

export default function PollsPage() {
  const [form, setForm] = useState(defaultPoll);
  const [optionTextByPoll, setOptionTextByPoll] = useState({});
  const [message, setMessage] = useState('');
  const polls = useSelector((state) => state.polls.items ?? []);
  const apiStatus = useSelector((state) => state.polls.status ?? 'idle');
  const apiError = useSelector((state) => state.polls.error);
  const dispatch = useDispatch();
  const totalVotes = useMemo(
    () => polls.reduce((sum, poll) => sum + getPollTotalVotes(poll), 0),
    [polls],
  );

  useEffect(() => {
    if (apiStatus === 'idle') {
      dispatch(fetchRemotePolls());
    }
  }, [apiStatus, dispatch]);

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function handleCreatePoll(event) {
    event.preventDefault();
    const options = [form.optionA.trim(), form.optionB.trim()].filter(Boolean);

    if (!form.title.trim() || options.length < 2) {
      setMessage('Вкажіть назву опитування та мінімум два варіанти відповідей.');
      return;
    }

    dispatch(
      addPoll({
        title: form.title.trim(),
        description: form.description.trim() || 'Без опису',
        options,
      }),
    );
    setForm(defaultPoll);
    setMessage('Опитування створено.');
  }

  function handleAddOption(event, pollId) {
    event.preventDefault();
    const text = optionTextByPoll[pollId]?.trim();

    if (!text) {
      return;
    }

    dispatch(addOption({ pollId, text }));
    setOptionTextByPoll((current) => ({ ...current, [pollId]: '' }));
  }

  return (
    <section className="container polls-page">
      <PageHeader eyebrow="Робоча сторінка" title="Конструктор опитувань">
        Створюйте питання, додавайте варіанти відповідей і переглядайте статистику голосування.
      </PageHeader>

      {apiStatus === 'loading' ? <div className="alert alert-secondary">Завантаження опитувань з API...</div> : null}
      {apiStatus === 'failed' ? (
        <div className="alert alert-warning">
          {apiError} Показано локальні дані з Redux/localStorage.
        </div>
      ) : null}

      <div className="stats-strip">
        <div>
          <span>{polls.length}</span>
          <p>опитувань</p>
        </div>
        <div>
          <span>{totalVotes}</span>
          <p>голосів</p>
        </div>
        <div>
          <span>localStorage</span>
          <p>сховище даних</p>
        </div>
      </div>

      <div className="polls-layout">
        <form className="surface-form create-poll-form" onSubmit={handleCreatePoll}>
          <h2>Нове опитування</h2>
          {message ? <div className="alert alert-info">{message}</div> : null}

          <label className="form-label" htmlFor="title">
            Назва питання
          </label>
          <input
            className="form-control"
            id="title"
            name="title"
            type="text"
            value={form.title}
            onChange={updateForm}
            placeholder="Наприклад: який стек обрати?"
          />

          <label className="form-label" htmlFor="description">
            Короткий опис
          </label>
          <textarea
            className="form-control"
            id="description"
            name="description"
            rows="3"
            value={form.description}
            onChange={updateForm}
            placeholder="Для чого потрібне це опитування"
          />

          <label className="form-label" htmlFor="optionA">
            Варіант відповіді 1
          </label>
          <input
            className="form-control"
            id="optionA"
            name="optionA"
            type="text"
            value={form.optionA}
            onChange={updateForm}
            placeholder="React"
          />

          <label className="form-label" htmlFor="optionB">
            Варіант відповіді 2
          </label>
          <input
            className="form-control"
            id="optionB"
            name="optionB"
            type="text"
            value={form.optionB}
            onChange={updateForm}
            placeholder="Vue"
          />

          <button className="btn btn-dark btn-lg w-100" type="submit">
            <Plus size={18} aria-hidden="true" />
            Створити
          </button>
        </form>

        <div className="poll-list">
          {polls.map((poll) => (
            <PollCard
              key={poll.id}
              poll={poll}
              optionText={optionTextByPoll[poll.id] ?? ''}
              onOptionTextChange={(value) =>
                setOptionTextByPoll((current) => ({ ...current, [poll.id]: value }))
              }
              onAddOption={(event) => handleAddOption(event, poll.id)}
              onVote={(optionId) => dispatch(vote({ pollId: poll.id, optionId }))}
              onRemove={() => dispatch(removePoll(poll.id))}
            />
          ))}
        </div>
      </div>
    </section>
  );
}

function PollCard({ poll, optionText, onOptionTextChange, onAddOption, onVote, onRemove }) {
  const total = getPollTotalVotes(poll);

  return (
    <article className="poll-card">
      <div className="poll-card-header">
        <div>
          <span className="poll-date">{poll.createdAt}</span>
          <h2>{poll.title}</h2>
          <p>{poll.description}</p>
        </div>
        <button className="icon-button" type="button" onClick={onRemove} aria-label="Видалити опитування">
          <Trash2 size={18} />
        </button>
      </div>

      <div className="options-list">
        {poll.options.map((option) => {
          const percent = total > 0 ? Math.round((option.votes / total) * 100) : 0;

          return (
            <div className="option-row" key={option.id}>
              <div className="d-flex justify-content-between gap-2">
                <strong>{option.text}</strong>
                <span>{option.votes} голосів</span>
              </div>
              <div className="progress" role="progressbar" aria-label={`Статистика: ${option.text}`}>
                <div className="progress-bar" style={{ width: `${percent}%` }}>
                  {percent}%
                </div>
              </div>
              <button className="btn btn-outline-dark btn-sm" type="button" onClick={() => onVote(option.id)}>
                <Vote size={16} aria-hidden="true" />
                Голосувати
              </button>
            </div>
          );
        })}
      </div>

      <form className="add-option-form" onSubmit={onAddOption}>
        <input
          className="form-control"
          type="text"
          value={optionText}
          onChange={(event) => onOptionTextChange(event.target.value)}
          placeholder="Додати ще один варіант"
          aria-label="Новий варіант відповіді"
        />
        <button className="btn btn-dark" type="submit" aria-label="Додати варіант">
          <Send size={17} />
        </button>
      </form>
    </article>
  );
}
