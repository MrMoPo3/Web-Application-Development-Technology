import { BarChart3, Database, ShieldCheck } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';

export default function AboutPage() {
  return (
    <section className="container page-narrow">
      <PageHeader eyebrow="Про додаток" title="PollCraft">
        Web-додаток для створення опитувань, керування варіантами відповідей і перегляду статистики голосів.
      </PageHeader>

      <div className="about-panel">
        <div className="app-emblem" aria-label="Емблема додатку PollCraft">
          <BarChart3 size={58} />
          <span>PC</span>
        </div>
        <div>
          <h2>Призначення</h2>
          <p>
            PollCraft моделює клієнтську частину API-сервісу опитувань. Користувач може створити опитування,
            додати варіанти відповідей, голосувати та бачити відсоткову статистику.
          </p>
          <div className="feature-list">
            <span>
              <Database size={18} /> Redux Toolkit + REST API
            </span>
            <span>
              <ShieldCheck size={18} /> Реєстрація та вхід
            </span>
            <span>
              <BarChart3 size={18} /> Статистика голосів
            </span>
          </div>
        </div>
      </div>
    </section>
  );
}
